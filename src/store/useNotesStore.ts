import { create } from 'zustand';
import { Category, ChecklistItem, Note, NoteType } from '@/types';
import {
  createEmptyNote,
  deleteNotePermanently,
  emptyTrash as emptyTrashDb,
  fetchAllNotes,
  upsertNote,
} from '@/db/notesRepository';
import {
  createCategory as createCategoryDb,
  deleteCategory as deleteCategoryDb,
  fetchAllCategories,
  updateCategory as updateCategoryDb,
} from '@/db/categoriesRepository';
import { cancelNoteReminder, nextMonthlyOccurrence, scheduleNoteReminder } from '@/utils/notifications';

interface NotesState {
  notes: Note[];
  categories: Category[];
  loading: boolean;
  selectedIds: string[];
  loadAll: () => Promise<void>;
  createNote: (type: NoteType, categoryId?: string | null) => Promise<Note>;
  saveNote: (note: Note) => Promise<void>;
  togglePinned: (id: string) => Promise<void>;
  toggleFavorite: (id: string) => Promise<void>;
  archiveNote: (id: string, archived: boolean) => Promise<void>;
  moveToTrash: (id: string) => Promise<void>;
  restoreFromTrash: (id: string) => Promise<void>;
  deleteForever: (id: string) => Promise<void>;
  emptyTrash: () => Promise<void>;
  setChecklistItemChecked: (noteId: string, itemId: string, checked: boolean) => Promise<void>;
  toggleSelect: (id: string) => void;
  clearSelection: () => void;
  selectAll: (ids: string[]) => void;
  addCategory: (name: string, color: string, icon: string) => Promise<Category>;
  updateCategory: (category: Category) => Promise<void>;
  removeCategory: (id: string) => Promise<void>;
}

export const useNotesStore = create<NotesState>((set, get) => ({
  notes: [],
  categories: [],
  loading: true,
  selectedIds: [],

  loadAll: async () => {
    set({ loading: true });
    const [rawNotes, categories] = await Promise.all([fetchAllNotes(), fetchAllCategories()]);
    const notes = await Promise.all(rawNotes.map(advanceOverdueMonthlyReminder));
    set({ notes, categories, loading: false });
  },

  createNote: async (type, categoryId = null) => {
    const note = createEmptyNote(type, categoryId);
    await upsertNote(note);
    set((state) => ({ notes: [note, ...state.notes] }));
    return note;
  },

  saveNote: async (note) => {
    const previous = get().notes.find((n) => n.id === note.id);
    const updated: Note = { ...note, updatedAt: Date.now() };

    const reminderChanged =
      previous &&
      (previous.reminderAt !== updated.reminderAt || previous.reminderRepeat !== updated.reminderRepeat);

    if (previous?.notificationId && reminderChanged) {
      await cancelNoteReminder(previous.notificationId);
      updated.notificationId = null;
    }
    if (updated.reminderAt && !updated.notificationId) {
      updated.notificationId = await scheduleNoteReminder(updated);
    }
    if (!updated.reminderAt && previous?.notificationId) {
      await cancelNoteReminder(previous.notificationId);
      updated.notificationId = null;
    }

    await upsertNote(updated);
    set((state) => ({
      notes: state.notes.some((n) => n.id === updated.id)
        ? state.notes.map((n) => (n.id === updated.id ? updated : n))
        : [updated, ...state.notes],
    }));
  },

  togglePinned: async (id) => {
    const note = get().notes.find((n) => n.id === id);
    if (!note) return;
    await get().saveNote({ ...note, pinned: !note.pinned });
  },

  toggleFavorite: async (id) => {
    const note = get().notes.find((n) => n.id === id);
    if (!note) return;
    await get().saveNote({ ...note, favorite: !note.favorite });
  },

  archiveNote: async (id, archived) => {
    const note = get().notes.find((n) => n.id === id);
    if (!note) return;
    await get().saveNote({ ...note, archived, pinned: archived ? false : note.pinned });
  },

  moveToTrash: async (id) => {
    const note = get().notes.find((n) => n.id === id);
    if (!note) return;
    if (note.notificationId) await cancelNoteReminder(note.notificationId);
    const updated: Note = {
      ...note,
      deleted: true,
      deletedAt: Date.now(),
      pinned: false,
      notificationId: null,
    };
    await upsertNote(updated);
    set((state) => ({ notes: state.notes.map((n) => (n.id === id ? updated : n)) }));
  },

  restoreFromTrash: async (id) => {
    const note = get().notes.find((n) => n.id === id);
    if (!note) return;
    await get().saveNote({ ...note, deleted: false, deletedAt: null });
  },

  deleteForever: async (id) => {
    await deleteNotePermanently(id);
    set((state) => ({ notes: state.notes.filter((n) => n.id !== id) }));
  },

  emptyTrash: async () => {
    await emptyTrashDb();
    set((state) => ({ notes: state.notes.filter((n) => !n.deleted) }));
  },

  setChecklistItemChecked: async (noteId, itemId, checked) => {
    const note = get().notes.find((n) => n.id === noteId);
    if (!note) return;
    const checklist: ChecklistItem[] = note.checklist.map((item) =>
      item.id === itemId ? { ...item, checked } : item
    );
    await get().saveNote({ ...note, checklist });
  },

  toggleSelect: (id) => {
    set((state) => ({
      selectedIds: state.selectedIds.includes(id)
        ? state.selectedIds.filter((s) => s !== id)
        : [...state.selectedIds, id],
    }));
  },

  clearSelection: () => set({ selectedIds: [] }),
  selectAll: (ids) => set({ selectedIds: ids }),

  addCategory: async (name, color, icon) => {
    const category = await createCategoryDb(name, color, icon);
    set((state) => ({ categories: [...state.categories, category] }));
    return category;
  },

  updateCategory: async (category) => {
    await updateCategoryDb(category);
    set((state) => ({
      categories: state.categories.map((c) => (c.id === category.id ? category : c)),
    }));
  },

  removeCategory: async (id) => {
    await deleteCategoryDb(id);
    set((state) => ({
      categories: state.categories.filter((c) => c.id !== id),
      notes: state.notes.map((n) => (n.categoryId === id ? { ...n, categoryId: null } : n)),
    }));
  },
}));

/** Monthly reminders have no native repeat trigger, so an overdue one is rolled forward on load. */
async function advanceOverdueMonthlyReminder(note: Note): Promise<Note> {
  if (note.reminderRepeat !== 'monthly' || !note.reminderAt || note.reminderAt > Date.now()) {
    return note;
  }
  const nextReminderAt = nextMonthlyOccurrence(note.reminderAt);
  const updated: Note = { ...note, reminderAt: nextReminderAt, notificationId: null };
  updated.notificationId = await scheduleNoteReminder(updated);
  await upsertNote(updated);
  return updated;
}

import { getDatabase } from '@/db/database';
import { generateId } from '@/utils/id';
import { ChecklistItem, Note, NoteType, ReminderRepeat } from '@/types';

interface NoteRow {
  id: string;
  type: string;
  title: string;
  content: string;
  checklist: string;
  color: string;
  categoryId: string | null;
  tags: string;
  favorite: number;
  pinned: number;
  archived: number;
  deleted: number;
  locked: number;
  reminderAt: number | null;
  reminderRepeat: string;
  notificationId: string | null;
  createdAt: number;
  updatedAt: number;
  deletedAt: number | null;
}

function rowToNote(row: NoteRow): Note {
  return {
    id: row.id,
    type: row.type as NoteType,
    title: row.title,
    content: row.content,
    checklist: safeParseChecklist(row.checklist),
    color: row.color,
    categoryId: row.categoryId,
    tags: safeParseTags(row.tags),
    favorite: !!row.favorite,
    pinned: !!row.pinned,
    archived: !!row.archived,
    deleted: !!row.deleted,
    locked: !!row.locked,
    reminderAt: row.reminderAt,
    reminderRepeat: row.reminderRepeat as ReminderRepeat,
    notificationId: row.notificationId,
    createdAt: row.createdAt,
    updatedAt: row.updatedAt,
    deletedAt: row.deletedAt,
  };
}

function safeParseChecklist(raw: string): ChecklistItem[] {
  try {
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function safeParseTags(raw: string): string[] {
  try {
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function createEmptyNote(type: NoteType, categoryId: string | null = null): Note {
  const now = Date.now();
  return {
    id: generateId(),
    type,
    title: '',
    content: '',
    checklist: [],
    color: 'default',
    categoryId,
    tags: [],
    favorite: false,
    pinned: false,
    archived: false,
    deleted: false,
    locked: false,
    reminderAt: null,
    reminderRepeat: 'none',
    notificationId: null,
    createdAt: now,
    updatedAt: now,
    deletedAt: null,
  };
}

export async function fetchAllNotes(): Promise<Note[]> {
  const db = await getDatabase();
  const rows = await db.getAllAsync<NoteRow>('SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC');
  return rows.map(rowToNote);
}

export async function fetchNoteById(id: string): Promise<Note | null> {
  const db = await getDatabase();
  const row = await db.getFirstAsync<NoteRow>('SELECT * FROM notes WHERE id = ?', id);
  return row ? rowToNote(row) : null;
}

export async function upsertNote(note: Note): Promise<void> {
  const db = await getDatabase();
  await db.runAsync(
    `INSERT INTO notes (
      id, type, title, content, checklist, color, categoryId, tags,
      favorite, pinned, archived, deleted, locked,
      reminderAt, reminderRepeat, notificationId, createdAt, updatedAt, deletedAt
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(id) DO UPDATE SET
      type=excluded.type, title=excluded.title, content=excluded.content,
      checklist=excluded.checklist, color=excluded.color, categoryId=excluded.categoryId,
      tags=excluded.tags, favorite=excluded.favorite, pinned=excluded.pinned,
      archived=excluded.archived, deleted=excluded.deleted, locked=excluded.locked,
      reminderAt=excluded.reminderAt, reminderRepeat=excluded.reminderRepeat,
      notificationId=excluded.notificationId, updatedAt=excluded.updatedAt,
      deletedAt=excluded.deletedAt`,
    note.id,
    note.type,
    note.title,
    note.content,
    JSON.stringify(note.checklist),
    note.color,
    note.categoryId,
    JSON.stringify(note.tags),
    note.favorite ? 1 : 0,
    note.pinned ? 1 : 0,
    note.archived ? 1 : 0,
    note.deleted ? 1 : 0,
    note.locked ? 1 : 0,
    note.reminderAt,
    note.reminderRepeat,
    note.notificationId,
    note.createdAt,
    note.updatedAt,
    note.deletedAt
  );
}

export async function deleteNotePermanently(id: string): Promise<void> {
  const db = await getDatabase();
  await db.runAsync('DELETE FROM notes WHERE id = ?', id);
}

export async function emptyTrash(): Promise<void> {
  const db = await getDatabase();
  await db.runAsync('DELETE FROM notes WHERE deleted = 1');
}

export async function replaceAllNotes(notes: Note[]): Promise<void> {
  const db = await getDatabase();
  await db.withTransactionAsync(async () => {
    await db.runAsync('DELETE FROM notes');
    for (const note of notes) {
      await upsertNote(note);
    }
  });
}

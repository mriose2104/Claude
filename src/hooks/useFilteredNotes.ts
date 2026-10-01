import { useMemo } from 'react';
import { Note, SortBy, SortDirection } from '@/types';
import { useNotesStore } from '@/store/useNotesStore';
import { useSettingsStore } from '@/store/useSettingsStore';
import { stripFormatting } from '@/utils/richText';

export interface NoteFilterOptions {
  query?: string;
  categoryId?: string | null;
  tag?: string | null;
  onlyFavorites?: boolean;
  onlyType?: Note['type'];
  scope: 'active' | 'archived' | 'trash';
}

function matchesQuery(note: Note, query: string, categoryName?: string): boolean {
  if (!query) return true;
  const q = query.toLowerCase();
  if (note.title.toLowerCase().includes(q)) return true;
  if (note.type === 'text' && stripFormatting(note.content).toLowerCase().includes(q)) return true;
  if (note.type === 'checklist' && note.checklist.some((i) => i.text.toLowerCase().includes(q))) return true;
  if (note.tags.some((t) => t.toLowerCase().includes(q))) return true;
  if (categoryName && categoryName.toLowerCase().includes(q)) return true;
  return false;
}

function sortNotes(notes: Note[], sortBy: SortBy, direction: SortDirection): Note[] {
  const sorted = [...notes].sort((a, b) => {
    let diff = 0;
    if (sortBy === 'title') diff = a.title.localeCompare(b.title);
    else if (sortBy === 'createdAt') diff = a.createdAt - b.createdAt;
    else if (sortBy === 'category') diff = (a.categoryId ?? '').localeCompare(b.categoryId ?? '');
    else diff = a.updatedAt - b.updatedAt;
    return direction === 'asc' ? diff : -diff;
  });
  return sorted;
}

export function useFilteredNotes(options: NoteFilterOptions): Note[] {
  const notes = useNotesStore((s) => s.notes);
  const categories = useNotesStore((s) => s.categories);
  const sortBy = useSettingsStore((s) => s.sortBy);
  const sortDirection = useSettingsStore((s) => s.sortDirection);

  return useMemo(() => {
    const categoryNameById = new Map(categories.map((c) => [c.id, c.name]));
    let result = notes.filter((note) => {
      if (options.scope === 'active' && (note.archived || note.deleted)) return false;
      if (options.scope === 'archived' && (!note.archived || note.deleted)) return false;
      if (options.scope === 'trash' && !note.deleted) return false;
      if (options.categoryId && note.categoryId !== options.categoryId) return false;
      if (options.tag && !note.tags.includes(options.tag)) return false;
      if (options.onlyFavorites && !note.favorite) return false;
      if (options.onlyType && note.type !== options.onlyType) return false;
      if (options.query && !matchesQuery(note, options.query, categoryNameById.get(note.categoryId ?? ''))) {
        return false;
      }
      return true;
    });

    result = sortNotes(result, sortBy, sortDirection);

    const pinned = result.filter((n) => n.pinned);
    const rest = result.filter((n) => !n.pinned);
    return options.scope === 'active' ? [...pinned, ...rest] : result;
  }, [notes, categories, options.scope, options.categoryId, options.tag, options.onlyFavorites, options.onlyType, options.query, sortBy, sortDirection]);
}

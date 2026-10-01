export type NoteType = 'text' | 'checklist';

export type ReminderRepeat = 'none' | 'daily' | 'weekly' | 'monthly';

export interface ChecklistItem {
  id: string;
  text: string;
  checked: boolean;
}

export interface Note {
  id: string;
  type: NoteType;
  title: string;
  /** Markdown-lite content for text notes (bold **, italic *, bullets "- ", checklist lines are ignored here). */
  content: string;
  /** Serialized ChecklistItem[] for checklist notes. */
  checklist: ChecklistItem[];
  color: string;
  categoryId: string | null;
  tags: string[];
  favorite: boolean;
  pinned: boolean;
  archived: boolean;
  deleted: boolean;
  locked: boolean;
  reminderAt: number | null;
  reminderRepeat: ReminderRepeat;
  notificationId: string | null;
  createdAt: number;
  updatedAt: number;
  deletedAt: number | null;
}

export interface Category {
  id: string;
  name: string;
  color: string;
  icon: string;
  isDefault: boolean;
  createdAt: number;
}

export type ViewMode = 'cards' | 'list';
export type SortBy = 'updatedAt' | 'createdAt' | 'title' | 'category';
export type SortDirection = 'asc' | 'desc';
export type ThemeMode = 'light' | 'dark' | 'system';
export type TextSize = 'small' | 'medium' | 'large';

export interface AppSettings {
  themeMode: ThemeMode;
  textSize: TextSize;
  viewMode: ViewMode;
  sortBy: SortBy;
  sortDirection: SortDirection;
  reminderSoundEnabled: boolean;
}

export interface SecuritySettings {
  pinEnabled: boolean;
  pinHash: string | null;
  biometricEnabled: boolean;
  lockOnExit: boolean;
}

export interface NoteFilter {
  query: string;
  categoryId: string | null;
  tag: string | null;
  onlyFavorites: boolean;
  onlyTasks: boolean;
  scope: 'active' | 'archived' | 'trash';
}

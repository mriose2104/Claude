import * as SQLite from 'expo-sqlite';
import { DEFAULT_CATEGORIES } from '@/constants/categories';

let dbInstance: SQLite.SQLiteDatabase | null = null;

const SCHEMA_SQL = `
PRAGMA journal_mode = WAL;

CREATE TABLE IF NOT EXISTS categories (
  id TEXT PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  color TEXT NOT NULL,
  icon TEXT NOT NULL,
  isDefault INTEGER NOT NULL DEFAULT 0,
  createdAt INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS notes (
  id TEXT PRIMARY KEY NOT NULL,
  type TEXT NOT NULL DEFAULT 'text',
  title TEXT NOT NULL DEFAULT '',
  content TEXT NOT NULL DEFAULT '',
  checklist TEXT NOT NULL DEFAULT '[]',
  color TEXT NOT NULL DEFAULT 'default',
  categoryId TEXT,
  tags TEXT NOT NULL DEFAULT '[]',
  favorite INTEGER NOT NULL DEFAULT 0,
  pinned INTEGER NOT NULL DEFAULT 0,
  archived INTEGER NOT NULL DEFAULT 0,
  deleted INTEGER NOT NULL DEFAULT 0,
  locked INTEGER NOT NULL DEFAULT 0,
  reminderAt INTEGER,
  reminderRepeat TEXT NOT NULL DEFAULT 'none',
  notificationId TEXT,
  createdAt INTEGER NOT NULL,
  updatedAt INTEGER NOT NULL,
  deletedAt INTEGER,
  FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_notes_pinned ON notes(pinned);
CREATE INDEX IF NOT EXISTS idx_notes_archived ON notes(archived);
CREATE INDEX IF NOT EXISTS idx_notes_deleted ON notes(deleted);
CREATE INDEX IF NOT EXISTS idx_notes_category ON notes(categoryId);
CREATE INDEX IF NOT EXISTS idx_notes_updatedAt ON notes(updatedAt);
`;

export async function getDatabase(): Promise<SQLite.SQLiteDatabase> {
  if (dbInstance) return dbInstance;
  dbInstance = await SQLite.openDatabaseAsync('notes_pro.db');
  await dbInstance.execAsync(SCHEMA_SQL);
  await seedDefaultCategories(dbInstance);
  return dbInstance;
}

async function seedDefaultCategories(db: SQLite.SQLiteDatabase): Promise<void> {
  const row = await db.getFirstAsync<{ count: number }>('SELECT COUNT(*) as count FROM categories');
  if (row && row.count > 0) return;

  const now = Date.now();
  for (const category of DEFAULT_CATEGORIES) {
    await db.runAsync(
      'INSERT INTO categories (id, name, color, icon, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?)',
      category.id,
      category.name,
      category.color,
      category.icon,
      category.isDefault ? 1 : 0,
      now
    );
  }
}

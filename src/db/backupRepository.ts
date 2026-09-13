import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import * as DocumentPicker from 'expo-document-picker';
import { Category, Note } from '@/types';
import { fetchAllNotes, replaceAllNotes, upsertNote } from '@/db/notesRepository';
import { fetchAllCategories } from '@/db/categoriesRepository';
import { getDatabase } from '@/db/database';

export interface BackupPayload {
  version: 1;
  exportedAt: number;
  notes: Note[];
  categories: Category[];
}

const BACKUP_DIR = FileSystem.documentDirectory + 'backups/';
const LAST_BACKUP_FILE = BACKUP_DIR + 'last_backup.json';

async function ensureBackupDir(): Promise<void> {
  const info = await FileSystem.getInfoAsync(BACKUP_DIR);
  if (!info.exists) {
    await FileSystem.makeDirectoryAsync(BACKUP_DIR, { intermediates: true });
  }
}

export async function buildBackupPayload(): Promise<BackupPayload> {
  const [notes, categories] = await Promise.all([fetchAllNotes(), fetchAllCategories()]);
  return { version: 1, exportedAt: Date.now(), notes, categories };
}

/** Writes a manual backup snapshot to app storage; used for local restore. */
export async function createManualBackup(): Promise<string> {
  await ensureBackupDir();
  const payload = await buildBackupPayload();
  await FileSystem.writeAsStringAsync(LAST_BACKUP_FILE, JSON.stringify(payload, null, 2));
  return LAST_BACKUP_FILE;
}

export async function hasLocalBackup(): Promise<boolean> {
  const info = await FileSystem.getInfoAsync(LAST_BACKUP_FILE);
  return info.exists;
}

export async function restoreFromLocalBackup(): Promise<number> {
  const info = await FileSystem.getInfoAsync(LAST_BACKUP_FILE);
  if (!info.exists) throw new Error('No hay una copia de seguridad local disponible.');
  const raw = await FileSystem.readAsStringAsync(LAST_BACKUP_FILE);
  const payload = JSON.parse(raw) as BackupPayload;
  await applyBackupPayload(payload, 'replace');
  return payload.notes.length;
}

/** Exports the current notes/categories as a shareable .json file via the OS share sheet. */
export async function exportNotesToFile(): Promise<void> {
  await ensureBackupDir();
  const payload = await buildBackupPayload();
  const fileName = `notes-pro-backup-${new Date().toISOString().slice(0, 10)}.json`;
  const filePath = BACKUP_DIR + fileName;
  await FileSystem.writeAsStringAsync(filePath, JSON.stringify(payload, null, 2));

  const canShare = await Sharing.isAvailableAsync();
  if (canShare) {
    await Sharing.shareAsync(filePath, {
      mimeType: 'application/json',
      dialogTitle: 'Exportar notas',
    });
  }
}

export type ImportMode = 'merge' | 'replace';

/** Lets the user pick a previously exported .json file and imports it. */
export async function importNotesFromFile(mode: ImportMode): Promise<number> {
  const result = await DocumentPicker.getDocumentAsync({
    type: ['application/json', 'text/plain', '*/*'],
    copyToCacheDirectory: true,
  });

  if (result.canceled || !result.assets || result.assets.length === 0) {
    return 0;
  }

  const raw = await FileSystem.readAsStringAsync(result.assets[0].uri);
  const payload = JSON.parse(raw) as BackupPayload;
  if (!payload || !Array.isArray(payload.notes)) {
    throw new Error('El archivo seleccionado no tiene un formato válido.');
  }

  await applyBackupPayload(payload, mode);
  return payload.notes.length;
}

async function applyBackupPayload(payload: BackupPayload, mode: ImportMode): Promise<void> {
  if (mode === 'replace') {
    await replaceAllNotes(payload.notes);
    return;
  }

  const db = await getDatabase();
  for (const category of payload.categories ?? []) {
    await db.runAsync(
      `INSERT INTO categories (id, name, color, icon, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?)
       ON CONFLICT(id) DO NOTHING`,
      category.id,
      category.name,
      category.color,
      category.icon,
      category.isDefault ? 1 : 0,
      category.createdAt
    );
  }
  for (const note of payload.notes) {
    await upsertNote(note);
  }
}

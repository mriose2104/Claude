import { getDatabase } from '@/db/database';
import { generateId } from '@/utils/id';
import { Category } from '@/types';

interface CategoryRow {
  id: string;
  name: string;
  color: string;
  icon: string;
  isDefault: number;
  createdAt: number;
}

function rowToCategory(row: CategoryRow): Category {
  return {
    id: row.id,
    name: row.name,
    color: row.color,
    icon: row.icon,
    isDefault: !!row.isDefault,
    createdAt: row.createdAt,
  };
}

export async function fetchAllCategories(): Promise<Category[]> {
  const db = await getDatabase();
  const rows = await db.getAllAsync<CategoryRow>('SELECT * FROM categories ORDER BY isDefault DESC, name ASC');
  return rows.map(rowToCategory);
}

export async function createCategory(name: string, color: string, icon: string): Promise<Category> {
  const db = await getDatabase();
  const category: Category = {
    id: generateId(),
    name,
    color,
    icon,
    isDefault: false,
    createdAt: Date.now(),
  };
  await db.runAsync(
    'INSERT INTO categories (id, name, color, icon, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?)',
    category.id,
    category.name,
    category.color,
    category.icon,
    0,
    category.createdAt
  );
  return category;
}

export async function updateCategory(category: Category): Promise<void> {
  const db = await getDatabase();
  await db.runAsync(
    'UPDATE categories SET name = ?, color = ?, icon = ? WHERE id = ?',
    category.name,
    category.color,
    category.icon,
    category.id
  );
}

export async function deleteCategory(id: string): Promise<void> {
  const db = await getDatabase();
  await db.withTransactionAsync(async () => {
    await db.runAsync('UPDATE notes SET categoryId = NULL WHERE categoryId = ?', id);
    await db.runAsync('DELETE FROM categories WHERE id = ?', id);
  });
}

import { Category } from '@/types';

/** Seeded on first launch; the user can rename, recolor, delete, or add their own. */
export const DEFAULT_CATEGORIES: Omit<Category, 'createdAt'>[] = [
  { id: 'personal', name: 'Personal', color: '#6C5CE7', icon: 'person', isDefault: true },
  { id: 'work', name: 'Trabajo', color: '#0984E3', icon: 'briefcase', isDefault: true },
  { id: 'shopping', name: 'Compras', color: '#00B894', icon: 'cart', isDefault: true },
  { id: 'ideas', name: 'Ideas', color: '#FDCB6E', icon: 'bulb', isDefault: true },
  { id: 'todo', name: 'Pendientes', color: '#E17055', icon: 'checkbox', isDefault: true },
  { id: 'important', name: 'Importante', color: '#D63031', icon: 'alert-circle', isDefault: true },
];

export const CATEGORY_ICON_CHOICES = [
  'person', 'briefcase', 'cart', 'bulb', 'checkbox', 'alert-circle',
  'home', 'heart', 'school', 'airplane', 'fitness', 'book',
  'cash', 'gift', 'musical-notes', 'restaurant', 'car', 'folder',
];

export const CATEGORY_COLOR_CHOICES = [
  '#6C5CE7', '#0984E3', '#00B894', '#FDCB6E', '#E17055', '#D63031',
  '#00CEC9', '#E84393', '#636E72', '#2D3436', '#FF7675', '#74B9FF',
];

export interface NoteColorSwatch {
  id: string;
  light: string;
  dark: string;
}

/** Palette of note accent colors, used for both light and dark theme rendering. */
export const NOTE_COLORS: NoteColorSwatch[] = [
  { id: 'default', light: '#FFFFFF', dark: '#2A2A32' },
  { id: 'red', light: '#FFD9D9', dark: '#4A2626' },
  { id: 'orange', light: '#FFE4C4', dark: '#4A3620' },
  { id: 'yellow', light: '#FFF3B0', dark: '#4A4420' },
  { id: 'green', light: '#D3F5D3', dark: '#234A26' },
  { id: 'teal', light: '#CFF5EC', dark: '#1F4A44' },
  { id: 'blue', light: '#D6E7FF', dark: '#22344A' },
  { id: 'purple', light: '#E6D9FF', dark: '#332347' },
  { id: 'pink', light: '#FFDCEE', dark: '#4A2340' },
  { id: 'gray', light: '#E4E4E8', dark: '#3A3A40' },
];

export function getNoteColorHex(colorId: string, scheme: 'light' | 'dark'): string {
  const swatch = NOTE_COLORS.find((c) => c.id === colorId) ?? NOTE_COLORS[0];
  return scheme === 'dark' ? swatch.dark : swatch.light;
}

export interface AppPalette {
  background: string;
  surface: string;
  surfaceElevated: string;
  card: string;
  border: string;
  text: string;
  textSecondary: string;
  textMuted: string;
  primary: string;
  primaryText: string;
  accent: string;
  danger: string;
  success: string;
  warning: string;
  tabBarBackground: string;
  overlay: string;
  chipBackground: string;
}

export const lightPalette: AppPalette = {
  background: '#F6F5FB',
  surface: '#FFFFFF',
  surfaceElevated: '#FFFFFF',
  card: '#FFFFFF',
  border: '#E8E6F2',
  text: '#1E1B2E',
  textSecondary: '#5A5670',
  textMuted: '#9491A8',
  primary: '#6C5CE7',
  primaryText: '#FFFFFF',
  accent: '#00B894',
  danger: '#E63946',
  success: '#00B894',
  warning: '#E17055',
  tabBarBackground: '#FFFFFF',
  overlay: 'rgba(30, 27, 46, 0.5)',
  chipBackground: '#EFEDFB',
};

export const darkPalette: AppPalette = {
  background: '#131217',
  surface: '#1C1B22',
  surfaceElevated: '#242229',
  card: '#201F26',
  border: '#302E38',
  text: '#F1F0F6',
  textSecondary: '#B7B4C7',
  textMuted: '#7C7A8C',
  primary: '#8C7CF0',
  primaryText: '#FFFFFF',
  accent: '#3DDCB0',
  danger: '#FF6B6B',
  success: '#3DDCB0',
  warning: '#FFA07A',
  tabBarBackground: '#1C1B22',
  overlay: 'rgba(0, 0, 0, 0.6)',
  chipBackground: '#2A2833',
};

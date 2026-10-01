import React, { createContext, useContext, useMemo } from 'react';
import { useColorScheme } from 'react-native';
import { AppPalette, darkPalette, lightPalette } from '@/theme/colors';
import { useSettingsStore } from '@/store/useSettingsStore';
import { TextSize } from '@/types';

interface ThemeContextValue {
  colors: AppPalette;
  isDark: boolean;
  fontScale: number;
  fontSizes: {
    xs: number;
    sm: number;
    md: number;
    lg: number;
    xl: number;
    xxl: number;
  };
}

const BASE_SIZES = { xs: 12, sm: 14, md: 16, lg: 18, xl: 22, xxl: 28 };
const SCALE_BY_TEXT_SIZE: Record<TextSize, number> = { small: 0.9, medium: 1, large: 1.15 };

const ThemeContext = createContext<ThemeContextValue>({
  colors: lightPalette,
  isDark: false,
  fontScale: 1,
  fontSizes: BASE_SIZES,
});

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const systemScheme = useColorScheme();
  const themeMode = useSettingsStore((s) => s.themeMode);
  const textSize = useSettingsStore((s) => s.textSize);

  const value = useMemo<ThemeContextValue>(() => {
    const isDark = themeMode === 'system' ? systemScheme === 'dark' : themeMode === 'dark';
    const fontScale = SCALE_BY_TEXT_SIZE[textSize];
    const fontSizes = Object.fromEntries(
      Object.entries(BASE_SIZES).map(([k, v]) => [k, Math.round(v * fontScale)])
    ) as ThemeContextValue['fontSizes'];

    return {
      colors: isDark ? darkPalette : lightPalette,
      isDark,
      fontScale,
      fontSizes,
    };
  }, [themeMode, systemScheme, textSize]);

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useAppTheme(): ThemeContextValue {
  return useContext(ThemeContext);
}

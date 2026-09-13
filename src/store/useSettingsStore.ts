import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AppSettings, SortBy, SortDirection, TextSize, ThemeMode, ViewMode } from '@/types';

const STORAGE_KEY = 'notes_pro_settings_v1';

const DEFAULT_SETTINGS: AppSettings = {
  themeMode: 'system',
  textSize: 'medium',
  viewMode: 'cards',
  sortBy: 'updatedAt',
  sortDirection: 'desc',
  reminderSoundEnabled: true,
};

interface SettingsState extends AppSettings {
  hydrated: boolean;
  hydrate: () => Promise<void>;
  setThemeMode: (mode: ThemeMode) => Promise<void>;
  setTextSize: (size: TextSize) => Promise<void>;
  setViewMode: (mode: ViewMode) => Promise<void>;
  setSortBy: (sortBy: SortBy) => Promise<void>;
  setSortDirection: (direction: SortDirection) => Promise<void>;
  setReminderSoundEnabled: (enabled: boolean) => Promise<void>;
}

async function persist(settings: AppSettings): Promise<void> {
  await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
}

export const useSettingsStore = create<SettingsState>((set, get) => ({
  ...DEFAULT_SETTINGS,
  hydrated: false,

  hydrate: async () => {
    const raw = await AsyncStorage.getItem(STORAGE_KEY);
    if (raw) {
      try {
        const parsed = JSON.parse(raw) as Partial<AppSettings>;
        set({ ...DEFAULT_SETTINGS, ...parsed, hydrated: true });
        return;
      } catch {
        // fall through to defaults
      }
    }
    set({ ...DEFAULT_SETTINGS, hydrated: true });
  },

  setThemeMode: async (themeMode) => {
    set({ themeMode });
    await persist(extractSettings(get()));
  },
  setTextSize: async (textSize) => {
    set({ textSize });
    await persist(extractSettings(get()));
  },
  setViewMode: async (viewMode) => {
    set({ viewMode });
    await persist(extractSettings(get()));
  },
  setSortBy: async (sortBy) => {
    set({ sortBy });
    await persist(extractSettings(get()));
  },
  setSortDirection: async (sortDirection) => {
    set({ sortDirection });
    await persist(extractSettings(get()));
  },
  setReminderSoundEnabled: async (reminderSoundEnabled) => {
    set({ reminderSoundEnabled });
    await persist(extractSettings(get()));
  },
}));

function extractSettings(state: SettingsState): AppSettings {
  const { themeMode, textSize, viewMode, sortBy, sortDirection, reminderSoundEnabled } = state;
  return { themeMode, textSize, viewMode, sortBy, sortDirection, reminderSoundEnabled };
}

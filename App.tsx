import React, { useEffect, useRef, useState } from 'react';
import { AppState, AppStateStatus } from 'react-native';
import { NavigationContainer, DefaultTheme, DarkTheme } from '@react-navigation/native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';

import { ThemeProvider, useAppTheme } from '@/theme/ThemeContext';
import { NoteUnlockProvider } from '@/security/NoteUnlockContext';
import { RootNavigator } from '@/navigation/RootNavigator';
import { LockScreen } from '@/screens/LockScreen';
import { getDatabase } from '@/db/database';
import { useNotesStore } from '@/store/useNotesStore';
import { useSettingsStore } from '@/store/useSettingsStore';
import { useSecurityStore } from '@/store/useSecurityStore';

function AppShell() {
  const { colors, isDark } = useAppTheme();
  const isLocked = useSecurityStore((s) => s.isLocked);
  const pinEnabled = useSecurityStore((s) => s.pinEnabled);
  const lockOnExit = useSecurityStore((s) => s.lockOnExit);
  const lock = useSecurityStore((s) => s.lock);
  const appState = useRef(AppState.currentState);

  useEffect(() => {
    const subscription = AppState.addEventListener('change', (next: AppStateStatus) => {
      if (appState.current === 'active' && next.match(/inactive|background/)) {
        if (pinEnabled && lockOnExit) lock();
      }
      appState.current = next;
    });
    return () => subscription.remove();
  }, [pinEnabled, lockOnExit, lock]);

  const navigationTheme = isDark
    ? { ...DarkTheme, colors: { ...DarkTheme.colors, background: colors.background, card: colors.surface, text: colors.text, border: colors.border, primary: colors.primary } }
    : { ...DefaultTheme, colors: { ...DefaultTheme.colors, background: colors.background, card: colors.surface, text: colors.text, border: colors.border, primary: colors.primary } };

  return (
    <NavigationContainer theme={navigationTheme}>
      <StatusBar style={isDark ? 'light' : 'dark'} />
      <NoteUnlockProvider>{isLocked ? <LockScreen /> : <RootNavigator />}</NoteUnlockProvider>
    </NavigationContainer>
  );
}

export default function App() {
  const [ready, setReady] = useState(false);
  const loadAll = useNotesStore((s) => s.loadAll);
  const hydrateSettings = useSettingsStore((s) => s.hydrate);
  const hydrateSecurity = useSecurityStore((s) => s.hydrate);

  useEffect(() => {
    (async () => {
      await getDatabase();
      await Promise.all([loadAll(), hydrateSettings(), hydrateSecurity()]);
      setReady(true);
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!ready) return null;

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <ThemeProvider>
          <AppShell />
        </ThemeProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

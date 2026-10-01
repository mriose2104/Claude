import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { PinPad } from '@/components/PinPad';
import { useAppTheme } from '@/theme/ThemeContext';
import { useSecurityStore } from '@/store/useSecurityStore';
import { authenticateWithBiometrics, verifyPin } from '@/utils/security';

type Resolver = (result: boolean) => void;

const NoteUnlockContext = createContext<() => Promise<boolean>>(async () => true);

export function NoteUnlockProvider({ children }: { children: React.ReactNode }) {
  const { colors, fontSizes } = useAppTheme();
  const { pinEnabled, pinHash, biometricEnabled } = useSecurityStore();
  const [visible, setVisible] = useState(false);
  const [pin, setPin] = useState('');
  const [error, setError] = useState(false);
  const resolverRef = useRef<Resolver | null>(null);

  const requestUnlock = useCallback((): Promise<boolean> => {
    if (!pinEnabled || !pinHash) return Promise.resolve(true);
    setPin('');
    setError(false);
    setVisible(true);
    return new Promise<boolean>((resolve) => {
      resolverRef.current = resolve;
    });
  }, [pinEnabled, pinHash]);

  const finish = useCallback((result: boolean) => {
    setVisible(false);
    resolverRef.current?.(result);
    resolverRef.current = null;
  }, []);

  const tryBiometric = useCallback(async () => {
    const ok = await authenticateWithBiometrics('Desbloquear nota protegida');
    if (ok) finish(true);
  }, [finish]);

  useEffect(() => {
    if (visible && biometricEnabled) {
      tryBiometric();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visible]);

  const handleDigit = async (digit: string) => {
    if (!pinHash) return;
    const next = (pin + digit).slice(0, 6);
    setPin(next);
    setError(false);
    if (next.length >= 4) {
      const ok = await verifyPin(next, pinHash);
      if (ok) {
        finish(true);
      } else if (next.length === 6) {
        setError(true);
        setTimeout(() => setPin(''), 400);
      }
    }
  };

  return (
    <NoteUnlockContext.Provider value={requestUnlock}>
      {children}
      <Modal visible={visible} transparent animationType="fade" onRequestClose={() => finish(false)}>
        <View style={[styles.backdrop, { backgroundColor: colors.overlay }]}>
          <View style={[styles.card, { backgroundColor: colors.surface }]}>
            <Text style={[styles.title, { color: colors.text, fontSize: fontSizes.lg }]}>Nota protegida</Text>
            <Text style={[styles.subtitle, { color: colors.textMuted, fontSize: fontSizes.sm }]}>
              Ingresa tu PIN para continuar
            </Text>
            <PinPad
              length={6}
              value={pin}
              onDigit={handleDigit}
              onBackspace={() => setPin((p) => p.slice(0, -1))}
              onBiometricPress={tryBiometric}
              showBiometric={biometricEnabled}
              error={error}
            />
            <Pressable style={styles.cancelButton} onPress={() => finish(false)}>
              <Text style={{ color: colors.primary, fontSize: fontSizes.sm, fontWeight: '600' }}>Cancelar</Text>
            </Pressable>
          </View>
        </View>
      </Modal>
    </NoteUnlockContext.Provider>
  );
}

export function useNoteUnlock(): () => Promise<boolean> {
  return useContext(NoteUnlockContext);
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  card: {
    borderRadius: 24,
    padding: 24,
    alignItems: 'center',
    width: '86%',
  },
  title: {
    fontWeight: '700',
    marginBottom: 4,
  },
  subtitle: {
    marginBottom: 20,
  },
  cancelButton: {
    marginTop: 16,
    padding: 8,
  },
});

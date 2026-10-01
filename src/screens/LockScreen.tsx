import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { PinPad } from '@/components/PinPad';
import { useAppTheme } from '@/theme/ThemeContext';
import { useSecurityStore } from '@/store/useSecurityStore';
import { authenticateWithBiometrics, verifyPin } from '@/utils/security';

export function LockScreen() {
  const { colors, fontSizes } = useAppTheme();
  const { pinHash, biometricEnabled, unlock } = useSecurityStore();
  const [pin, setPin] = useState('');
  const [error, setError] = useState(false);

  const tryBiometric = async () => {
    const ok = await authenticateWithBiometrics('Desbloquea Notes Pro');
    if (ok) unlock();
  };

  useEffect(() => {
    if (biometricEnabled) tryBiometric();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleDigit = async (digit: string) => {
    if (!pinHash) return;
    const next = (pin + digit).slice(0, 6);
    setPin(next);
    setError(false);
    if (next.length >= 4) {
      const ok = await verifyPin(next, pinHash);
      if (ok) {
        unlock();
      } else if (next.length === 6) {
        setError(true);
        setTimeout(() => setPin(''), 400);
      }
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.logo, { backgroundColor: colors.primary }]}>
        <Ionicons name="albums" size={32} color="#fff" />
      </View>
      <Text style={{ color: colors.text, fontSize: fontSizes.xl, fontWeight: '800', marginBottom: 4 }}>
        Notes Pro
      </Text>
      <Text style={{ color: colors.textMuted, fontSize: fontSizes.sm, marginBottom: 36 }}>
        Ingresa tu PIN para continuar
      </Text>
      <PinPad
        length={6}
        value={pin}
        error={error}
        onDigit={handleDigit}
        onBackspace={() => setPin((p) => p.slice(0, -1))}
        onBiometricPress={tryBiometric}
        showBiometric={biometricEnabled}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  logo: {
    width: 68,
    height: 68,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
});

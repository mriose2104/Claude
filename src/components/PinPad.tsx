import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  length: number;
  value: string;
  onDigit: (digit: string) => void;
  onBackspace: () => void;
  onBiometricPress?: () => void;
  showBiometric?: boolean;
  error?: boolean;
}

const KEYS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', 'bio', '0', 'back'];

export function PinPad({ length, value, onDigit, onBackspace, onBiometricPress, showBiometric, error }: Props) {
  const { colors, fontSizes } = useAppTheme();

  return (
    <View style={styles.container}>
      <View style={styles.dotsRow}>
        {Array.from({ length }).map((_, i) => (
          <View
            key={i}
            style={[
              styles.dot,
              {
                borderColor: error ? colors.danger : colors.primary,
                backgroundColor: i < value.length ? (error ? colors.danger : colors.primary) : 'transparent',
              },
            ]}
          />
        ))}
      </View>

      <View style={styles.grid}>
        {KEYS.map((key) => {
          if (key === 'bio') {
            return (
              <Pressable
                key={key}
                style={styles.key}
                disabled={!showBiometric}
                onPress={() => {
                  Haptics.selectionAsync();
                  onBiometricPress?.();
                }}
              >
                {showBiometric && <Ionicons name="finger-print" size={26} color={colors.primary} />}
              </Pressable>
            );
          }
          if (key === 'back') {
            return (
              <Pressable
                key={key}
                style={styles.key}
                onPress={() => {
                  Haptics.selectionAsync();
                  onBackspace();
                }}
              >
                <Ionicons name="backspace-outline" size={24} color={colors.text} />
              </Pressable>
            );
          }
          return (
            <Pressable
              key={key}
              style={({ pressed }) => [styles.key, pressed && { backgroundColor: colors.chipBackground }]}
              onPress={() => {
                Haptics.selectionAsync();
                onDigit(key);
              }}
            >
              <Text style={{ color: colors.text, fontSize: fontSizes.xl, fontWeight: '600' }}>{key}</Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
  },
  dotsRow: {
    flexDirection: 'row',
    gap: 14,
    marginBottom: 32,
  },
  dot: {
    width: 16,
    height: 16,
    borderRadius: 8,
    borderWidth: 2,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    width: 260,
    justifyContent: 'center',
  },
  key: {
    width: 80,
    height: 64,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

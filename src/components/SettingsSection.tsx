import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useAppTheme } from '@/theme/ThemeContext';

export function SettingsSection({ title, children }: { title: string; children: React.ReactNode }) {
  const { colors, fontSizes } = useAppTheme();
  return (
    <View style={styles.container}>
      <Text style={[styles.title, { color: colors.textMuted, fontSize: fontSizes.xs }]}>{title.toUpperCase()}</Text>
      <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.border }]}>{children}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 20,
  },
  title: {
    fontWeight: '700',
    marginBottom: 8,
    marginLeft: 4,
    letterSpacing: 0.5,
  },
  card: {
    borderRadius: 16,
    borderWidth: StyleSheet.hairlineWidth,
    paddingHorizontal: 14,
  },
});

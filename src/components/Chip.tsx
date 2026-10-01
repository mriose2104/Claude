import React from 'react';
import { Pressable, StyleSheet, Text } from 'react-native';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  label: string;
  active: boolean;
  onPress: () => void;
  color?: string;
}

export function Chip({ label, active, onPress, color }: Props) {
  const { colors, fontSizes } = useAppTheme();
  const activeColor = color ?? colors.primary;
  return (
    <Pressable
      onPress={onPress}
      style={[
        styles.chip,
        {
          backgroundColor: active ? activeColor : colors.chipBackground,
          borderColor: active ? activeColor : colors.border,
        },
      ]}
    >
      <Text
        style={{
          color: active ? '#fff' : colors.textSecondary,
          fontSize: fontSizes.sm,
          fontWeight: active ? '700' : '500',
        }}
      >
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  chip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: StyleSheet.hairlineWidth,
    marginRight: 8,
  },
});

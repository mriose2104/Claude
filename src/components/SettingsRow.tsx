import React from 'react';
import { Pressable, StyleSheet, Switch, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  value?: string;
  onPress?: () => void;
  switchValue?: boolean;
  onSwitchChange?: (value: boolean) => void;
  danger?: boolean;
}

export function SettingsRow({ icon, label, value, onPress, switchValue, onSwitchChange, danger }: Props) {
  const { colors, fontSizes } = useAppTheme();
  const hasSwitch = onSwitchChange !== undefined;

  const content = (
    <View style={styles.row}>
      <View style={[styles.iconWrap, { backgroundColor: colors.chipBackground }]}>
        <Ionicons name={icon} size={18} color={danger ? colors.danger : colors.primary} />
      </View>
      <Text style={{ flex: 1, color: danger ? colors.danger : colors.text, fontSize: fontSizes.sm, fontWeight: '500' }}>
        {label}
      </Text>
      {hasSwitch ? (
        <Switch value={!!switchValue} onValueChange={onSwitchChange} trackColor={{ true: colors.primary }} />
      ) : (
        <View style={styles.valueWrap}>
          {value && <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs, marginRight: 4 }}>{value}</Text>}
          {onPress && <Ionicons name="chevron-forward" size={16} color={colors.textMuted} />}
        </View>
      )}
    </View>
  );

  if (hasSwitch) return content;
  return (
    <Pressable onPress={onPress} style={({ pressed }) => pressed && { opacity: 0.6 }}>
      {content}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    gap: 12,
  },
  iconWrap: {
    width: 34,
    height: 34,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  valueWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
});

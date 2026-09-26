import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAppTheme } from '@/theme/ThemeContext';

interface Action {
  key: string;
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  onPress: () => void;
  danger?: boolean;
}

interface Props {
  count: number;
  actions: Action[];
  onCancel: () => void;
}

export function SelectionToolbar({ count, actions, onCancel }: Props) {
  const { colors, fontSizes } = useAppTheme();
  return (
    <View style={[styles.container, { backgroundColor: colors.surfaceElevated, borderTopColor: colors.border }]}>
      <View style={styles.header}>
        <Pressable onPress={onCancel} hitSlop={8}>
          <Ionicons name="close" size={22} color={colors.text} />
        </Pressable>
        <Text style={[styles.count, { color: colors.text, fontSize: fontSizes.sm }]}>
          {count} seleccionada{count === 1 ? '' : 's'}
        </Text>
      </View>
      <View style={styles.actions}>
        {actions.map((action) => (
          <Pressable key={action.key} style={styles.actionButton} onPress={action.onPress}>
            <Ionicons name={action.icon} size={22} color={action.danger ? colors.danger : colors.text} />
            <Text
              style={{
                color: action.danger ? colors.danger : colors.textSecondary,
                fontSize: fontSizes.xs,
                marginTop: 2,
              }}
            >
              {action.label}
            </Text>
          </Pressable>
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    paddingTop: 10,
    paddingBottom: 20,
    paddingHorizontal: 16,
    borderTopWidth: StyleSheet.hairlineWidth,
    elevation: 12,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 8,
  },
  count: {
    fontWeight: '600',
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  actionButton: {
    alignItems: 'center',
    minWidth: 56,
  },
});

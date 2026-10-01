import React from 'react';
import { Pressable, StyleSheet, TextInput, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  onFilterPress?: () => void;
  filterActive?: boolean;
}

export function SearchBar({ value, onChangeText, placeholder, onFilterPress, filterActive }: Props) {
  const { colors, fontSizes } = useAppTheme();
  return (
    <View style={[styles.container, { backgroundColor: colors.surface, borderColor: colors.border }]}>
      <Ionicons name="search" size={18} color={colors.textMuted} />
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder ?? 'Buscar notas, etiquetas...'}
        placeholderTextColor={colors.textMuted}
        style={[styles.input, { color: colors.text, fontSize: fontSizes.sm }]}
        returnKeyType="search"
      />
      {value.length > 0 && (
        <Pressable onPress={() => onChangeText('')} hitSlop={8}>
          <Ionicons name="close-circle" size={18} color={colors.textMuted} />
        </Pressable>
      )}
      {onFilterPress && (
        <Pressable onPress={onFilterPress} hitSlop={8} style={styles.filterButton}>
          <Ionicons
            name="options"
            size={20}
            color={filterActive ? colors.primary : colors.textMuted}
          />
        </Pressable>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 14,
    borderWidth: StyleSheet.hairlineWidth,
    paddingHorizontal: 12,
    height: 44,
    gap: 8,
  },
  input: {
    flex: 1,
    height: '100%',
  },
  filterButton: {
    paddingLeft: 6,
    marginLeft: 2,
    borderLeftWidth: StyleSheet.hairlineWidth,
  },
});

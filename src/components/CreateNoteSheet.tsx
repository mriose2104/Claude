import React from 'react';
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAppTheme } from '@/theme/ThemeContext';
import { NoteType } from '@/types';

interface Option {
  key: string;
  type: NoteType;
  label: string;
  description: string;
  icon: keyof typeof Ionicons.glyphMap;
  quick?: boolean;
}

const OPTIONS: Option[] = [
  { key: 'text', type: 'text', label: 'Nota de texto', description: 'Escribe con formato básico', icon: 'document-text' },
  { key: 'tasks', type: 'checklist', label: 'Lista de tareas', description: 'Organiza pendientes por hacer', icon: 'list' },
  { key: 'checklist', type: 'checklist', label: 'Nota con checklist', description: 'Ítems marcables dentro de una nota', icon: 'checkbox' },
  { key: 'quick', type: 'text', label: 'Nota rápida', description: 'Captura una idea al instante', icon: 'flash', quick: true },
];

interface Props {
  visible: boolean;
  onClose: () => void;
  onSelect: (type: NoteType, quick: boolean) => void;
}

export function CreateNoteSheet({ visible, onClose, onSelect }: Props) {
  const { colors, fontSizes } = useAppTheme();

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={onClose}>
        <Pressable style={[styles.sheet, { backgroundColor: colors.surface }]} onPress={(e) => e.stopPropagation()}>
          <View style={[styles.handle, { backgroundColor: colors.border }]} />
          <Text style={[styles.title, { color: colors.text, fontSize: fontSizes.lg }]}>Nueva nota</Text>
          {OPTIONS.map((option) => (
            <Pressable
              key={option.key}
              style={({ pressed }) => [styles.option, { opacity: pressed ? 0.7 : 1 }]}
              onPress={() => onSelect(option.type, !!option.quick)}
            >
              <View style={[styles.iconWrap, { backgroundColor: colors.chipBackground }]}>
                <Ionicons name={option.icon} size={22} color={colors.primary} />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={{ color: colors.text, fontSize: fontSizes.md, fontWeight: '600' }}>
                  {option.label}
                </Text>
                <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs }}>
                  {option.description}
                </Text>
              </View>
              <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
            </Pressable>
          ))}
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  sheet: {
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 20,
    paddingBottom: 32,
  },
  handle: {
    width: 40,
    height: 4,
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 16,
  },
  title: {
    fontWeight: '700',
    marginBottom: 12,
  },
  option: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    gap: 12,
  },
  iconWrap: {
    width: 44,
    height: 44,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

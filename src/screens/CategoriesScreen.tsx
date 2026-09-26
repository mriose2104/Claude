import React, { useState } from 'react';
import { Alert, FlatList, Modal, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { FAB } from '@/components/FAB';
import { useAppTheme } from '@/theme/ThemeContext';
import { useNotesStore } from '@/store/useNotesStore';
import { CATEGORY_COLOR_CHOICES, CATEGORY_ICON_CHOICES } from '@/constants/categories';
import { Category } from '@/types';

export function CategoriesScreen() {
  const { colors, fontSizes } = useAppTheme();
  const navigation = useNavigation<any>();
  const categories = useNotesStore((s) => s.categories);
  const notes = useNotesStore((s) => s.notes);
  const addCategory = useNotesStore((s) => s.addCategory);
  const updateCategory = useNotesStore((s) => s.updateCategory);
  const removeCategory = useNotesStore((s) => s.removeCategory);

  const [editing, setEditing] = useState<Category | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [name, setName] = useState('');
  const [color, setColor] = useState(CATEGORY_COLOR_CHOICES[0]);
  const [icon, setIcon] = useState(CATEGORY_ICON_CHOICES[0]);

  const countFor = (id: string) => notes.filter((n) => n.categoryId === id && !n.deleted).length;

  const openCreate = () => {
    setEditing(null);
    setName('');
    setColor(CATEGORY_COLOR_CHOICES[0]);
    setIcon(CATEGORY_ICON_CHOICES[0]);
    setModalVisible(true);
  };

  const openEdit = (category: Category) => {
    setEditing(category);
    setName(category.name);
    setColor(category.color);
    setIcon(category.icon);
    setModalVisible(true);
  };

  const handleSave = async () => {
    const trimmed = name.trim();
    if (!trimmed) return;
    if (editing) {
      await updateCategory({ ...editing, name: trimmed, color, icon });
    } else {
      await addCategory(trimmed, color, icon);
    }
    setModalVisible(false);
  };

  const handleDelete = (category: Category) => {
    Alert.alert('Eliminar categoría', `Las notas de "${category.name}" quedarán sin categoría.`, [
      { text: 'Cancelar', style: 'cancel' },
      { text: 'Eliminar', style: 'destructive', onPress: () => removeCategory(category.id) },
    ]);
  };

  return (
    <ScreenContainer>
      <ScreenHeader title="Categorías" subtitle={`${categories.length} categorías`} />
      <FlatList
        data={categories}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        renderItem={({ item }) => (
          <Pressable
            style={[styles.row, { backgroundColor: colors.surface, borderColor: colors.border }]}
            onPress={() => navigation.navigate('CategoryNotes', { categoryId: item.id, categoryName: item.name })}
            onLongPress={() => openEdit(item)}
          >
            <View style={[styles.iconWrap, { backgroundColor: item.color + '22' }]}>
              <Ionicons name={item.icon as any} size={20} color={item.color} />
            </View>
            <View style={{ flex: 1 }}>
              <Text style={{ color: colors.text, fontSize: fontSizes.md, fontWeight: '600' }}>{item.name}</Text>
              <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs }}>{countFor(item.id)} notas</Text>
            </View>
            <Pressable onPress={() => openEdit(item)} hitSlop={8} style={styles.iconButton}>
              <Ionicons name="create-outline" size={20} color={colors.textMuted} />
            </Pressable>
            <Pressable onPress={() => handleDelete(item)} hitSlop={8} style={styles.iconButton}>
              <Ionicons name="trash-outline" size={20} color={colors.danger} />
            </Pressable>
          </Pressable>
        )}
      />
      <FAB onPress={openCreate} icon="add" />

      <Modal visible={modalVisible} transparent animationType="fade" onRequestClose={() => setModalVisible(false)}>
        <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={() => setModalVisible(false)}>
          <Pressable style={[styles.modalCard, { backgroundColor: colors.surface }]} onPress={(e) => e.stopPropagation()}>
            <Text style={{ color: colors.text, fontSize: fontSizes.lg, fontWeight: '700', marginBottom: 12 }}>
              {editing ? 'Editar categoría' : 'Nueva categoría'}
            </Text>
            <TextInput
              value={name}
              onChangeText={setName}
              placeholder="Nombre de la categoría"
              placeholderTextColor={colors.textMuted}
              style={[styles.input, { color: colors.text, borderColor: colors.border }]}
            />
            <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs, marginTop: 12, marginBottom: 6 }}>
              Color
            </Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false}>
              {CATEGORY_COLOR_CHOICES.map((c) => (
                <Pressable
                  key={c}
                  onPress={() => setColor(c)}
                  style={[
                    styles.colorDot,
                    { backgroundColor: c, borderWidth: color === c ? 3 : 0, borderColor: colors.text },
                  ]}
                />
              ))}
            </ScrollView>
            <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs, marginTop: 12, marginBottom: 6 }}>
              Ícono
            </Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false}>
              {CATEGORY_ICON_CHOICES.map((ic) => (
                <Pressable
                  key={ic}
                  onPress={() => setIcon(ic)}
                  style={[
                    styles.iconChoice,
                    {
                      backgroundColor: icon === ic ? color + '33' : colors.chipBackground,
                      borderColor: icon === ic ? color : 'transparent',
                    },
                  ]}
                >
                  <Ionicons name={ic as any} size={20} color={icon === ic ? color : colors.textMuted} />
                </Pressable>
              ))}
            </ScrollView>
            <View style={styles.modalActions}>
              <Pressable style={styles.modalButton} onPress={() => setModalVisible(false)}>
                <Text style={{ color: colors.textMuted, fontSize: fontSizes.sm }}>Cancelar</Text>
              </Pressable>
              <Pressable style={[styles.modalButton, { backgroundColor: colors.primary, borderRadius: 12 }]} onPress={handleSave}>
                <Text style={{ color: '#fff', fontSize: fontSizes.sm, fontWeight: '700' }}>Guardar</Text>
              </Pressable>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  listContent: { paddingHorizontal: 16, paddingBottom: 100 },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    borderRadius: 16,
    borderWidth: StyleSheet.hairlineWidth,
    marginBottom: 10,
    gap: 12,
  },
  iconWrap: {
    width: 42,
    height: 42,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  iconButton: {
    padding: 4,
  },
  backdrop: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalCard: {
    width: '88%',
    borderRadius: 20,
    padding: 20,
  },
  input: {
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  colorDot: {
    width: 32,
    height: 32,
    borderRadius: 16,
    marginRight: 10,
  },
  iconChoice: {
    width: 40,
    height: 40,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 10,
    borderWidth: 1,
  },
  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 20,
    gap: 12,
  },
  modalButton: {
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
});

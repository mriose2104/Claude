import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  Alert,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import DateTimePicker from '@react-native-community/datetimepicker';
import { RouteProp, useNavigation, useRoute } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import * as Sharing from 'expo-sharing';
import * as FileSystem from 'expo-file-system';
import { ScreenContainer } from '@/components/ScreenContainer';
import { Chip } from '@/components/Chip';
import { useAppTheme } from '@/theme/ThemeContext';
import { useNotesStore } from '@/store/useNotesStore';
import { useSecurityStore } from '@/store/useSecurityStore';
import { RootStackParamList } from '@/navigation/types';
import { ChecklistItem, Note, ReminderRepeat } from '@/types';
import { NOTE_COLORS, getNoteColorHex } from '@/constants/noteColors';
import { generateId } from '@/utils/id';
import { findMatchRange, numberLines, prefixLines, stripFormatting, wrapSelection } from '@/utils/richText';
import { formatFullDateTime } from '@/utils/dateUtils';

type NavProp = NativeStackNavigationProp<RootStackParamList>;

export function NoteEditorScreen() {
  const { colors, isDark, fontSizes } = useAppTheme();
  const navigation = useNavigation<NavProp>();
  const route = useRoute<RouteProp<RootStackParamList, 'NoteEditor'>>();
  const noteId = route.params.noteId;

  const storeNote = useNotesStore((s) => s.notes.find((n) => n.id === noteId));
  const categories = useNotesStore((s) => s.categories);
  const saveNote = useNotesStore((s) => s.saveNote);
  const archiveNote = useNotesStore((s) => s.archiveNote);
  const moveToTrash = useNotesStore((s) => s.moveToTrash);
  const deleteForever = useNotesStore((s) => s.deleteForever);
  const pinEnabled = useSecurityStore((s) => s.pinEnabled);

  const [note, setNote] = useState<Note | null>(storeNote ?? null);
  const [selection, setSelection] = useState({ start: 0, end: 0 });
  const [tagDraft, setTagDraft] = useState('');
  const [colorPickerOpen, setColorPickerOpen] = useState(false);
  const [categoryPickerOpen, setCategoryPickerOpen] = useState(false);
  const [reminderModalOpen, setReminderModalOpen] = useState(false);
  const dirtyRef = useRef(false);
  const noteRef = useRef(note);
  const initialEmptyRef = useRef(
    storeNote ? isNoteEmpty(storeNote) : true
  );
  const contentInputRef = useRef<TextInput>(null);
  const checklistInputRefs = useRef(new Map<string, TextInput>());
  const jumpedToSearchRef = useRef(false);

  useEffect(() => {
    noteRef.current = note;
  }, [note]);

  useEffect(() => {
    if (storeNote && !dirtyRef.current) {
      setNote(storeNote);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [storeNote?.id]);

  useEffect(() => {
    if (!note || !dirtyRef.current) return;
    const timeout = setTimeout(() => {
      saveNote(note);
      dirtyRef.current = false;
    }, 500);
    return () => clearTimeout(timeout);
  }, [note]);

  useEffect(() => {
    const searchQuery = route.params.searchQuery;
    if (!note || !searchQuery || jumpedToSearchRef.current) return;
    jumpedToSearchRef.current = true;

    if (note.type === 'checklist') {
      const item = note.checklist.find((i) => i.text.toLowerCase().includes(searchQuery.toLowerCase()));
      if (!item) return;
      setTimeout(() => {
        const input = checklistInputRefs.current.get(item.id);
        const range = findMatchRange(item.text, searchQuery);
        input?.focus();
        if (range) input?.setNativeProps({ selection: range });
      }, 400);
      return;
    }

    if (note.type === 'text') {
      const range = findMatchRange(note.content, searchQuery);
      if (!range) return;
      setTimeout(() => {
        contentInputRef.current?.focus();
        contentInputRef.current?.setNativeProps({ selection: range });
      }, 400);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [note?.id]);

  useEffect(() => {
    return () => {
      const current = noteRef.current;
      if (!current) return;
      if (isNoteEmpty(current) && initialEmptyRef.current) {
        deleteForever(current.id);
        return;
      }
      if (dirtyRef.current) {
        saveNote(current);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!note) return null;

  const update = (patch: Partial<Note>) => {
    dirtyRef.current = true;
    setNote((prev) => (prev ? { ...prev, ...patch } : prev));
  };

  const bg = getNoteColorHex(note.color, isDark ? 'dark' : 'light');
  const category = categories.find((c) => c.id === note.categoryId);

  const applyFormatting = (
    kind: 'bold' | 'italic' | 'underline' | 'strikethrough' | 'heading' | 'bullet' | 'numbered' | 'checkbox'
  ) => {
    const { start, end } = selection;
    switch (kind) {
      case 'bold':
        update({ content: wrapSelection(note.content, start, end, '**').text });
        break;
      case 'italic':
        update({ content: wrapSelection(note.content, start, end, '*').text });
        break;
      case 'underline':
        update({ content: wrapSelection(note.content, start, end, '__').text });
        break;
      case 'strikethrough':
        update({ content: wrapSelection(note.content, start, end, '~~').text });
        break;
      case 'heading':
        update({ content: prefixLines(note.content, start, end, '# ').text });
        break;
      case 'bullet':
        update({ content: prefixLines(note.content, start, end, '- ').text });
        break;
      case 'numbered':
        update({ content: numberLines(note.content, start, end).text });
        break;
      case 'checkbox':
        update({ content: prefixLines(note.content, start, end, '☐ ').text });
        break;
    }
  };

  const addChecklistItem = () => {
    const item: ChecklistItem = { id: generateId(), text: '', checked: false };
    update({ checklist: [...note.checklist, item] });
  };

  const updateChecklistItem = (id: string, patch: Partial<ChecklistItem>) => {
    update({ checklist: note.checklist.map((i) => (i.id === id ? { ...i, ...patch } : i)) });
  };

  const removeChecklistItem = (id: string) => {
    update({ checklist: note.checklist.filter((i) => i.id !== id) });
  };

  const addTag = () => {
    const tag = tagDraft.trim().replace(/^#/, '');
    if (!tag || note.tags.includes(tag)) {
      setTagDraft('');
      return;
    }
    update({ tags: [...note.tags, tag] });
    setTagDraft('');
  };

  const removeTag = (tag: string) => update({ tags: note.tags.filter((t) => t !== tag) });

  const handleToggleLock = () => {
    if (!note.locked && !pinEnabled) {
      Alert.alert(
        'Configura un PIN primero',
        'Para proteger notas individuales, activa un PIN en Configuración > Seguridad.'
      );
      return;
    }
    update({ locked: !note.locked });
  };

  const handleDelete = () => {
    Alert.alert('Eliminar nota', '¿Mover esta nota a la papelera?', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Eliminar',
        style: 'destructive',
        onPress: async () => {
          dirtyRef.current = false;
          await moveToTrash(note.id);
          navigation.goBack();
        },
      },
    ]);
  };

  const handleShare = async () => {
    const text = note.type === 'checklist'
      ? note.checklist.map((i) => `${i.checked ? '[x]' : '[ ]'} ${i.text}`).join('\n')
      : stripFormatting(note.content);
    const path = FileSystem.cacheDirectory + `${note.title || 'nota'}.txt`;
    await FileSystem.writeAsStringAsync(path, `${note.title}\n\n${text}`);
    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(path, { mimeType: 'text/plain' });
    }
  };

  return (
    <ScreenContainer>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={{ flex: 1 }}>
        <View style={styles.topBar}>
          <Pressable onPress={() => navigation.goBack()} hitSlop={10}>
            <Ionicons name="arrow-back" size={24} color={colors.text} />
          </Pressable>
          <View style={styles.topActions}>
            <Pressable onPress={() => update({ pinned: !note.pinned })} hitSlop={8} style={styles.topIcon}>
              <Ionicons name={note.pinned ? 'pin' : 'pin-outline'} size={22} color={note.pinned ? colors.warning : colors.text} />
            </Pressable>
            <Pressable onPress={() => update({ favorite: !note.favorite })} hitSlop={8} style={styles.topIcon}>
              <Ionicons name={note.favorite ? 'heart' : 'heart-outline'} size={22} color={note.favorite ? colors.danger : colors.text} />
            </Pressable>
            <Pressable onPress={handleToggleLock} hitSlop={8} style={styles.topIcon}>
              <Ionicons name={note.locked ? 'lock-closed' : 'lock-open-outline'} size={22} color={colors.text} />
            </Pressable>
            <Pressable onPress={handleShare} hitSlop={8} style={styles.topIcon}>
              <Ionicons name="share-social-outline" size={22} color={colors.text} />
            </Pressable>
            <Pressable onPress={() => archiveNote(note.id, !note.archived)} hitSlop={8} style={styles.topIcon}>
              <Ionicons name={note.archived ? 'archive' : 'archive-outline'} size={22} color={colors.text} />
            </Pressable>
            <Pressable onPress={handleDelete} hitSlop={8} style={styles.topIcon}>
              <Ionicons name="trash-outline" size={22} color={colors.danger} />
            </Pressable>
          </View>
        </View>

        <ScrollView style={[styles.editorArea, { backgroundColor: bg }]} contentContainerStyle={styles.editorContent} keyboardShouldPersistTaps="handled">
          <TextInput
            value={note.title}
            onChangeText={(title) => update({ title })}
            placeholder="Título"
            placeholderTextColor={colors.textMuted}
            style={[styles.titleInput, { color: colors.text, fontSize: fontSizes.xl }]}
            multiline
          />

          <View style={styles.metaRow}>
            <Pressable style={styles.metaChip} onPress={() => setCategoryPickerOpen(true)}>
              <Ionicons name="pricetag" size={14} color={category?.color ?? colors.textMuted} />
              <Text style={{ color: category?.color ?? colors.textMuted, fontSize: fontSizes.xs, marginLeft: 4 }}>
                {category?.name ?? 'Sin categoría'}
              </Text>
            </Pressable>
            <Pressable style={styles.metaChip} onPress={() => setReminderModalOpen(true)}>
              <Ionicons name="alarm-outline" size={14} color={note.reminderAt ? colors.primary : colors.textMuted} />
              <Text style={{ color: note.reminderAt ? colors.primary : colors.textMuted, fontSize: fontSizes.xs, marginLeft: 4 }}>
                {note.reminderAt ? formatFullDateTime(note.reminderAt) : 'Recordatorio'}
              </Text>
            </Pressable>
          </View>

          {note.locked ? (
            <Text style={{ color: colors.textMuted, fontStyle: 'italic', marginTop: 20 }}>
              Esta nota está protegida. El contenido se ocultará en notificaciones y vistas previas.
            </Text>
          ) : note.type === 'checklist' ? (
            <View style={styles.checklistContainer}>
              {note.checklist.map((item) => (
                <View key={item.id} style={styles.checklistRow}>
                  <Pressable onPress={() => updateChecklistItem(item.id, { checked: !item.checked })} hitSlop={8}>
                    <Ionicons
                      name={item.checked ? 'checkbox' : 'square-outline'}
                      size={22}
                      color={item.checked ? colors.textMuted : colors.primary}
                    />
                  </Pressable>
                  <TextInput
                    ref={(r) => {
                      if (r) checklistInputRefs.current.set(item.id, r);
                      else checklistInputRefs.current.delete(item.id);
                    }}
                    value={item.text}
                    onChangeText={(text) => updateChecklistItem(item.id, { text })}
                    placeholder="Elemento"
                    placeholderTextColor={colors.textMuted}
                    style={[
                      styles.checklistInput,
                      {
                        color: colors.text,
                        fontSize: fontSizes.md,
                        textDecorationLine: item.checked ? 'line-through' : 'none',
                      },
                    ]}
                  />
                  <Pressable onPress={() => removeChecklistItem(item.id)} hitSlop={8}>
                    <Ionicons name="close" size={18} color={colors.textMuted} />
                  </Pressable>
                </View>
              ))}
              <Pressable style={styles.addItemButton} onPress={addChecklistItem}>
                <Ionicons name="add-circle-outline" size={20} color={colors.primary} />
                <Text style={{ color: colors.primary, marginLeft: 6, fontSize: fontSizes.sm }}>Agregar elemento</Text>
              </Pressable>
            </View>
          ) : (
            <TextInput
              ref={contentInputRef}
              value={note.content}
              onChangeText={(content) => update({ content })}
              onSelectionChange={(e) => setSelection(e.nativeEvent.selection)}
              placeholder="Escribe tu nota..."
              placeholderTextColor={colors.textMuted}
              style={[styles.contentInput, { color: colors.text, fontSize: fontSizes.md }]}
              multiline
              textAlignVertical="top"
            />
          )}

          <View style={styles.tagsRow}>
            {note.tags.map((tag) => (
              <View key={tag} style={[styles.tagChip, { backgroundColor: colors.chipBackground }]}>
                <Text style={{ color: colors.textSecondary, fontSize: fontSizes.xs }}>#{tag}</Text>
                <Pressable onPress={() => removeTag(tag)} hitSlop={6} style={{ marginLeft: 4 }}>
                  <Ionicons name="close" size={12} color={colors.textMuted} />
                </Pressable>
              </View>
            ))}
            <TextInput
              value={tagDraft}
              onChangeText={setTagDraft}
              onSubmitEditing={addTag}
              placeholder="+ etiqueta"
              placeholderTextColor={colors.textMuted}
              style={{ color: colors.text, fontSize: fontSizes.xs, minWidth: 80 }}
              returnKeyType="done"
            />
          </View>
        </ScrollView>

        {!note.locked && note.type === 'text' && (
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            style={[styles.toolbar, { backgroundColor: colors.surfaceElevated, borderTopColor: colors.border }]}
            contentContainerStyle={styles.toolbarContent}
          >
            <ToolbarButton onPress={() => applyFormatting('bold')} label="B" bold />
            <ToolbarButton onPress={() => applyFormatting('italic')} label="I" italic />
            <ToolbarButton onPress={() => applyFormatting('underline')} label="U" underline />
            <ToolbarButton onPress={() => applyFormatting('strikethrough')} label="S" strikethrough />
            <ToolbarDivider />
            <ToolbarButton icon="text" onPress={() => applyFormatting('heading')} />
            <ToolbarButton icon="list" onPress={() => applyFormatting('bullet')} />
            <ToolbarButton icon="reorder-four" onPress={() => applyFormatting('numbered')} />
            <ToolbarButton icon="checkbox-outline" onPress={() => applyFormatting('checkbox')} />
            <ToolbarDivider />
            <ToolbarButton icon="color-palette-outline" onPress={() => setColorPickerOpen(true)} />
          </ScrollView>
        )}
        {(note.type === 'checklist' || note.locked) && (
          <View style={[styles.toolbar, { backgroundColor: colors.surfaceElevated, borderTopColor: colors.border }]}>
            <ToolbarButton icon="color-palette-outline" onPress={() => setColorPickerOpen(true)} />
          </View>
        )}
      </KeyboardAvoidingView>

      <ColorPickerModal
        visible={colorPickerOpen}
        current={note.color}
        onSelect={(color) => {
          update({ color });
          setColorPickerOpen(false);
        }}
        onClose={() => setColorPickerOpen(false)}
      />
      <CategoryPickerModal
        visible={categoryPickerOpen}
        current={note.categoryId}
        onSelect={(categoryId) => {
          update({ categoryId });
          setCategoryPickerOpen(false);
        }}
        onClose={() => setCategoryPickerOpen(false)}
      />
      <ReminderModal
        visible={reminderModalOpen}
        reminderAt={note.reminderAt}
        reminderRepeat={note.reminderRepeat}
        onSave={(reminderAt, reminderRepeat) => {
          update({ reminderAt, reminderRepeat });
          setReminderModalOpen(false);
        }}
        onClose={() => setReminderModalOpen(false)}
      />
    </ScreenContainer>
  );
}

function isNoteEmpty(note: Note): boolean {
  return (
    note.title.trim().length === 0 &&
    note.content.trim().length === 0 &&
    note.checklist.every((i) => i.text.trim().length === 0)
  );
}

function ToolbarButton({
  icon,
  onPress,
  label,
  bold,
  italic,
  underline,
  strikethrough,
}: {
  icon?: keyof typeof Ionicons.glyphMap;
  onPress: () => void;
  label?: string;
  bold?: boolean;
  italic?: boolean;
  underline?: boolean;
  strikethrough?: boolean;
}) {
  const { colors, fontSizes } = useAppTheme();
  if (label) {
    const textDecorationLine =
      underline && strikethrough ? 'underline line-through' : underline ? 'underline' : strikethrough ? 'line-through' : 'none';
    return (
      <Pressable style={styles.toolbarButton} onPress={onPress}>
        <Text
          style={{
            color: colors.text,
            fontSize: fontSizes.md,
            fontWeight: bold ? '800' : '600',
            fontStyle: italic ? 'italic' : 'normal',
            textDecorationLine,
          }}
        >
          {label}
        </Text>
      </Pressable>
    );
  }
  return (
    <Pressable style={styles.toolbarButton} onPress={onPress}>
      <Ionicons name={icon ?? 'ellipse'} size={20} color={colors.text} />
    </Pressable>
  );
}

function ToolbarDivider() {
  const { colors } = useAppTheme();
  return <View style={[styles.toolbarDivider, { backgroundColor: colors.border }]} />;
}

function ColorPickerModal({
  visible,
  current,
  onSelect,
  onClose,
}: {
  visible: boolean;
  current: string;
  onSelect: (color: string) => void;
  onClose: () => void;
}) {
  const { colors, isDark } = useAppTheme();
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={onClose}>
        <Pressable style={[styles.sheetCard, { backgroundColor: colors.surface }]} onPress={(e) => e.stopPropagation()}>
          <Text style={{ color: colors.text, fontWeight: '700', marginBottom: 12 }}>Color de la nota</Text>
          <View style={styles.colorGrid}>
            {NOTE_COLORS.map((swatch) => (
              <Pressable
                key={swatch.id}
                onPress={() => onSelect(swatch.id)}
                style={[
                  styles.colorSwatch,
                  {
                    backgroundColor: isDark ? swatch.dark : swatch.light,
                    borderWidth: current === swatch.id ? 3 : 1,
                    borderColor: current === swatch.id ? colors.primary : colors.border,
                  },
                ]}
              />
            ))}
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

function CategoryPickerModal({
  visible,
  current,
  onSelect,
  onClose,
}: {
  visible: boolean;
  current: string | null;
  onSelect: (categoryId: string | null) => void;
  onClose: () => void;
}) {
  const { colors, fontSizes } = useAppTheme();
  const categories = useNotesStore((s) => s.categories);
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={onClose}>
        <Pressable style={[styles.sheetCard, { backgroundColor: colors.surface }]} onPress={(e) => e.stopPropagation()}>
          <Text style={{ color: colors.text, fontWeight: '700', marginBottom: 12 }}>Categoría</Text>
          <ScrollView style={{ maxHeight: 320 }}>
            <Pressable style={styles.categoryOption} onPress={() => onSelect(null)}>
              <Text style={{ color: current === null ? colors.primary : colors.text, fontSize: fontSizes.sm }}>
                Sin categoría
              </Text>
            </Pressable>
            {categories.map((cat) => (
              <Pressable key={cat.id} style={styles.categoryOption} onPress={() => onSelect(cat.id)}>
                <Text style={{ color: current === cat.id ? cat.color : colors.text, fontSize: fontSizes.sm, fontWeight: current === cat.id ? '700' : '400' }}>
                  {cat.name}
                </Text>
              </Pressable>
            ))}
          </ScrollView>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const REPEAT_OPTIONS: { value: ReminderRepeat; label: string }[] = [
  { value: 'none', label: 'Ninguna' },
  { value: 'daily', label: 'Diaria' },
  { value: 'weekly', label: 'Semanal' },
  { value: 'monthly', label: 'Mensual' },
];

function ReminderModal({
  visible,
  reminderAt,
  reminderRepeat,
  onSave,
  onClose,
}: {
  visible: boolean;
  reminderAt: number | null;
  reminderRepeat: ReminderRepeat;
  onSave: (reminderAt: number | null, repeat: ReminderRepeat) => void;
  onClose: () => void;
}) {
  const { colors, fontSizes } = useAppTheme();
  const [date, setDate] = useState<Date>(reminderAt ? new Date(reminderAt) : new Date(Date.now() + 30 * 60 * 1000));
  const [repeat, setRepeat] = useState<ReminderRepeat>(reminderRepeat);
  const [pickerMode, setPickerMode] = useState<'date' | 'time' | null>(null);

  useEffect(() => {
    if (visible) {
      setDate(reminderAt ? new Date(reminderAt) : new Date(Date.now() + 30 * 60 * 1000));
      setRepeat(reminderRepeat);
    }
  }, [visible, reminderAt, reminderRepeat]);

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={onClose}>
        <Pressable style={[styles.sheetCard, { backgroundColor: colors.surface }]} onPress={(e) => e.stopPropagation()}>
          <Text style={{ color: colors.text, fontWeight: '700', marginBottom: 12, fontSize: fontSizes.lg }}>Recordatorio</Text>

          <View style={styles.reminderRow}>
            <Pressable style={[styles.reminderField, { borderColor: colors.border }]} onPress={() => setPickerMode('date')}>
              <Ionicons name="calendar-outline" size={16} color={colors.textMuted} />
              <Text style={{ color: colors.text, marginLeft: 8 }}>{date.toLocaleDateString('es-ES')}</Text>
            </Pressable>
            <Pressable style={[styles.reminderField, { borderColor: colors.border }]} onPress={() => setPickerMode('time')}>
              <Ionicons name="time-outline" size={16} color={colors.textMuted} />
              <Text style={{ color: colors.text, marginLeft: 8 }}>
                {date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
              </Text>
            </Pressable>
          </View>

          {pickerMode && (
            <DateTimePicker
              value={date}
              mode={pickerMode}
              is24Hour
              display={Platform.OS === 'ios' ? 'spinner' : 'default'}
              onChange={(_, selectedDate) => {
                setPickerMode(null);
                if (selectedDate) setDate(selectedDate);
              }}
            />
          )}

          <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs, marginTop: 16, marginBottom: 6 }}>
            Repetición
          </Text>
          <View style={{ flexDirection: 'row', flexWrap: 'wrap' }}>
            {REPEAT_OPTIONS.map((opt) => (
              <Chip key={opt.value} label={opt.label} active={repeat === opt.value} onPress={() => setRepeat(opt.value)} />
            ))}
          </View>

          <View style={styles.modalActions}>
            {reminderAt !== null && (
              <Pressable style={styles.modalButton} onPress={() => onSave(null, 'none')}>
                <Text style={{ color: colors.danger, fontSize: fontSizes.sm }}>Quitar</Text>
              </Pressable>
            )}
            <Pressable style={styles.modalButton} onPress={onClose}>
              <Text style={{ color: colors.textMuted, fontSize: fontSizes.sm }}>Cancelar</Text>
            </Pressable>
            <Pressable
              style={[styles.modalButton, { backgroundColor: colors.primary, borderRadius: 12 }]}
              onPress={() => onSave(date.getTime(), repeat)}
            >
              <Text style={{ color: '#fff', fontSize: fontSizes.sm, fontWeight: '700' }}>Guardar</Text>
            </Pressable>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  topActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  topIcon: {
    marginLeft: 14,
  },
  editorArea: {
    flex: 1,
  },
  editorContent: {
    padding: 20,
    paddingBottom: 40,
  },
  titleInput: {
    fontWeight: '800',
    marginBottom: 8,
  },
  metaRow: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 16,
  },
  metaChip: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  contentInput: {
    minHeight: 200,
    lineHeight: 22,
  },
  checklistContainer: {
    marginTop: 4,
  },
  checklistRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
    gap: 10,
  },
  checklistInput: {
    flex: 1,
    paddingVertical: 4,
  },
  addItemButton: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 6,
    paddingVertical: 8,
  },
  tagsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    marginTop: 24,
    gap: 8,
  },
  tagChip: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 12,
  },
  toolbar: {
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  toolbarContent: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    paddingHorizontal: 8,
  },
  toolbarButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    minWidth: 40,
    alignItems: 'center',
  },
  toolbarDivider: {
    width: StyleSheet.hairlineWidth,
    height: 24,
    marginHorizontal: 4,
  },
  backdrop: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sheetCard: {
    width: '88%',
    borderRadius: 20,
    padding: 20,
  },
  colorGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  colorSwatch: {
    width: 40,
    height: 40,
    borderRadius: 20,
  },
  categoryOption: {
    paddingVertical: 10,
  },
  reminderRow: {
    flexDirection: 'row',
    gap: 12,
  },
  reminderField: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 10,
    flex: 1,
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

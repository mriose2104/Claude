import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Note } from '@/types';
import { useAppTheme } from '@/theme/ThemeContext';
import { getNoteColorHex } from '@/constants/noteColors';
import { formatRelativeDate } from '@/utils/dateUtils';
import { FormattedText } from '@/components/FormattedText';
import { HighlightedInlineText } from '@/components/HighlightedInlineText';
import { useNotesStore } from '@/store/useNotesStore';

interface Props {
  note: Note;
  viewMode: 'cards' | 'list';
  selected: boolean;
  selectionMode: boolean;
  onPress: () => void;
  onLongPress: () => void;
  /** Active search term, used to highlight matches in the title/preview. */
  highlightQuery?: string;
}

export function NoteCard({ note, viewMode, selected, selectionMode, onPress, onLongPress, highlightQuery }: Props) {
  const { colors, isDark, fontSizes } = useAppTheme();
  const categories = useNotesStore((s) => s.categories);
  const category = categories.find((c) => c.id === note.categoryId);
  const bg = getNoteColorHex(note.color, isDark ? 'dark' : 'light');

  const isList = viewMode === 'list';

  return (
    <Pressable
      onPress={onPress}
      onLongPress={onLongPress}
      style={({ pressed }) => [
        styles.card,
        isList ? styles.listCard : styles.gridCard,
        {
          backgroundColor: bg,
          borderColor: selected ? colors.primary : colors.border,
          borderWidth: selected ? 2 : StyleSheet.hairlineWidth,
          opacity: pressed ? 0.85 : 1,
        },
      ]}
    >
      {selectionMode && (
        <View style={styles.checkOverlay}>
          <Ionicons
            name={selected ? 'checkmark-circle' : 'ellipse-outline'}
            size={22}
            color={selected ? colors.primary : colors.textMuted}
          />
        </View>
      )}

      <View style={styles.headerRow}>
        {note.title.length > 0 && (
          <HighlightedInlineText
            text={note.title}
            highlight={highlightQuery}
            color={colors.text}
            style={[styles.title, { fontSize: fontSizes.md }]}
            numberOfLines={1}
          />
        )}
        <View style={styles.badges}>
          {note.locked && <Ionicons name="lock-closed" size={14} color={colors.textMuted} />}
          {note.reminderAt !== null && (
            <Ionicons name="alarm" size={14} color={colors.primary} style={styles.badgeIcon} />
          )}
          {note.pinned && (
            <Ionicons name="pin" size={14} color={colors.warning} style={styles.badgeIcon} />
          )}
        </View>
      </View>

      {note.locked ? (
        <Text style={[styles.lockedHint, { color: colors.textMuted, fontSize: fontSizes.sm }]}>
          Contenido protegido
        </Text>
      ) : note.type === 'checklist' ? (
        <ChecklistPreview note={note} textColor={colors.text} mutedColor={colors.textMuted} highlightQuery={highlightQuery} />
      ) : (
        <FormattedText
          content={note.content}
          color={colors.textSecondary}
          fontSize={fontSizes.sm}
          numberOfLines={4}
          highlight={highlightQuery}
        />
      )}

      <View style={styles.footerRow}>
        <Text style={[styles.dateText, { color: colors.textMuted, fontSize: fontSizes.xs }]}>
          {formatRelativeDate(note.updatedAt)}
        </Text>
        {category && (
          <View style={[styles.categoryPill, { backgroundColor: category.color + '22' }]}>
            <Text style={[styles.categoryText, { color: category.color, fontSize: fontSizes.xs }]} numberOfLines={1}>
              {category.name}
            </Text>
          </View>
        )}
        {note.favorite && <Ionicons name="heart" size={13} color={colors.danger} />}
      </View>
    </Pressable>
  );
}

function ChecklistPreview({
  note,
  textColor,
  mutedColor,
  highlightQuery,
}: {
  note: Note;
  textColor: string;
  mutedColor: string;
  highlightQuery?: string;
}) {
  const { fontSizes } = useAppTheme();
  const maxItems = 4;
  const matchIndex = highlightQuery
    ? note.checklist.findIndex((i) => i.text.toLowerCase().includes(highlightQuery.toLowerCase()))
    : -1;
  const start = matchIndex > 0 ? Math.min(matchIndex, Math.max(note.checklist.length - maxItems, 0)) : 0;
  const items = note.checklist.slice(start, start + maxItems);
  const remaining = note.checklist.length - (start + items.length);
  return (
    <View>
      {items.map((item) => (
        <View key={item.id} style={styles.checklistRow}>
          <Ionicons
            name={item.checked ? 'checkbox' : 'square-outline'}
            size={14}
            color={item.checked ? mutedColor : textColor}
          />
          <HighlightedInlineText
            text={item.text || 'Elemento sin título'}
            highlight={highlightQuery}
            color={item.checked ? mutedColor : textColor}
            style={{
              fontSize: fontSizes.sm,
              marginLeft: 6,
              textDecorationLine: item.checked ? 'line-through' : 'none',
              flexShrink: 1,
            }}
            numberOfLines={1}
          />
        </View>
      ))}
      {remaining > 0 && (
        <Text style={{ color: mutedColor, fontSize: fontSizes.xs, marginTop: 2 }}>
          +{remaining} más
        </Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 18,
    padding: 14,
    marginBottom: 12,
  },
  gridCard: {
    flex: 1,
    margin: 6,
    minHeight: 130,
  },
  listCard: {
    width: '100%',
  },
  checkOverlay: {
    position: 'absolute',
    top: 10,
    right: 10,
    zIndex: 2,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  title: {
    fontWeight: '700',
    flexShrink: 1,
    marginRight: 8,
  },
  badges: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  badgeIcon: {
    marginLeft: 4,
  },
  lockedHint: {
    fontStyle: 'italic',
    marginTop: 4,
  },
  checklistRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 2,
  },
  footerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 10,
    gap: 8,
  },
  dateText: {
    flex: 1,
  },
  categoryPill: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 10,
    maxWidth: 100,
  },
  categoryText: {
    fontWeight: '600',
  },
});

import React from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { FAB } from '@/components/FAB';
import { CreateNoteSheet } from '@/components/CreateNoteSheet';
import { Chip } from '@/components/Chip';
import { EmptyState } from '@/components/EmptyState';
import { NoteCard } from '@/components/NoteCard';
import { useAppTheme } from '@/theme/ThemeContext';
import { useFilteredNotes } from '@/hooks/useFilteredNotes';
import { useCreateNoteFlow } from '@/hooks/useCreateNoteFlow';
import { useOpenNote } from '@/hooks/useOpenNote';
import { useNotesStore } from '@/store/useNotesStore';
import { useNavigation } from '@react-navigation/native';

export function HomeScreen() {
  const { colors, fontSizes } = useAppTheme();
  const navigation = useNavigation<any>();
  const allActive = useFilteredNotes({ scope: 'active' });
  const pinned = allActive.filter((n) => n.pinned);
  const recent = allActive.slice(0, 8);
  const categories = useNotesStore((s) => s.categories);
  const { sheetVisible, openSheet, closeSheet, handleSelect } = useCreateNoteFlow();
  const openNote = useOpenNote();

  return (
    <ScreenContainer>
      <ScreenHeader title="Inicio" subtitle={`${allActive.length} notas activas`} />
      <ScrollView contentContainerStyle={styles.scroll}>
        <View style={styles.quickRow}>
          {categories.slice(0, 6).map((cat) => (
            <Chip
              key={cat.id}
              label={cat.name}
              active={false}
              color={cat.color}
              onPress={() => navigation.navigate('CategoryNotes', { categoryId: cat.id, categoryName: cat.name })}
            />
          ))}
        </View>

        {pinned.length > 0 && (
          <>
            <SectionTitle text="Fijadas" />
            {pinned.map((note) => (
              <View key={note.id} style={styles.fullWidthCard}>
                <NoteCard
                  note={note}
                  viewMode="list"
                  selected={false}
                  selectionMode={false}
                  onPress={() => openNote(note)}
                  onLongPress={() => navigation.navigate('Notas')}
                />
              </View>
            ))}
          </>
        )}

        <SectionTitle text="Recientes" />
        {recent.length === 0 ? (
          <EmptyState
            icon="document-text-outline"
            title="Aún no tienes notas"
            subtitle="Toca el botón + para crear tu primera nota"
          />
        ) : (
          recent.map((note) => (
            <View key={note.id} style={styles.fullWidthCard}>
              <NoteCard
                note={note}
                viewMode="list"
                selected={false}
                selectionMode={false}
                onPress={() => openNote(note)}
                onLongPress={() => navigation.navigate('Notas')}
              />
            </View>
          ))
        )}
      </ScrollView>
      <FAB onPress={openSheet} />
      <CreateNoteSheet visible={sheetVisible} onClose={closeSheet} onSelect={handleSelect} />
    </ScreenContainer>
  );
}

function SectionTitle({ text }: { text: string }) {
  const { colors, fontSizes } = useAppTheme();
  return (
    <Text style={[styles.sectionTitle, { color: colors.text, fontSize: fontSizes.md }]}>{text}</Text>
  );
}

const styles = StyleSheet.create({
  scroll: {
    paddingHorizontal: 16,
    paddingBottom: 100,
  },
  quickRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginBottom: 12,
    gap: 4,
  },
  sectionTitle: {
    fontWeight: '700',
    marginTop: 16,
    marginBottom: 8,
  },
  fullWidthCard: {
    width: '100%',
  },
});

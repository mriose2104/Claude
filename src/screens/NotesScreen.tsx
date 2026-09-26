import React, { useState } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Pressable } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { SearchBar } from '@/components/SearchBar';
import { Chip } from '@/components/Chip';
import { NotesGrid } from '@/components/NotesGrid';
import { FAB } from '@/components/FAB';
import { CreateNoteSheet } from '@/components/CreateNoteSheet';
import { useAppTheme } from '@/theme/ThemeContext';
import { useFilteredNotes } from '@/hooks/useFilteredNotes';
import { useCreateNoteFlow } from '@/hooks/useCreateNoteFlow';
import { useNotesStore } from '@/store/useNotesStore';
import { useSettingsStore } from '@/store/useSettingsStore';

export function NotesScreen() {
  const { colors } = useAppTheme();
  const [query, setQuery] = useState('');
  const [categoryId, setCategoryId] = useState<string | null>(null);
  const [onlyFavorites, setOnlyFavorites] = useState(false);
  const categories = useNotesStore((s) => s.categories);
  const viewMode = useSettingsStore((s) => s.viewMode);
  const setViewMode = useSettingsStore((s) => s.setViewMode);
  const { sheetVisible, openSheet, closeSheet, handleSelect } = useCreateNoteFlow(categoryId);

  const notes = useFilteredNotes({ scope: 'active', query, categoryId, onlyFavorites });

  return (
    <ScreenContainer>
      <ScreenHeader
        title="Notas"
        right={
          <Pressable
            onPress={() => setViewMode(viewMode === 'cards' ? 'list' : 'cards')}
            style={[styles.toggleButton, { backgroundColor: colors.chipBackground }]}
          >
            <Ionicons name={viewMode === 'cards' ? 'list' : 'grid'} size={20} color={colors.text} />
          </Pressable>
        }
      />
      <View style={styles.searchWrap}>
        <SearchBar value={query} onChangeText={setQuery} />
      </View>
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipsRow} contentContainerStyle={styles.chipsContent}>
        <Chip label="Todas" active={!categoryId && !onlyFavorites} onPress={() => { setCategoryId(null); setOnlyFavorites(false); }} />
        <Chip label="Favoritas" active={onlyFavorites} onPress={() => setOnlyFavorites((v) => !v)} />
        {categories.map((cat) => (
          <Chip
            key={cat.id}
            label={cat.name}
            color={cat.color}
            active={categoryId === cat.id}
            onPress={() => setCategoryId((current) => (current === cat.id ? null : cat.id))}
          />
        ))}
      </ScrollView>

      <NotesGrid
        notes={notes}
        viewMode={viewMode}
        mode="active"
        emptyIcon="search"
        emptyTitle={query ? 'Sin resultados' : 'No hay notas aquí'}
        emptySubtitle={query ? 'Intenta con otro término de búsqueda' : 'Crea una nota nueva con el botón +'}
        searchQuery={query}
      />

      <FAB onPress={openSheet} />
      <CreateNoteSheet visible={sheetVisible} onClose={closeSheet} onSelect={handleSelect} />
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  searchWrap: {
    paddingHorizontal: 16,
    marginBottom: 10,
  },
  chipsRow: {
    maxHeight: 44,
    marginBottom: 8,
  },
  chipsContent: {
    paddingHorizontal: 16,
  },
  toggleButton: {
    width: 40,
    height: 40,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

import React from 'react';
import { StyleSheet, View } from 'react-native';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { SearchBar } from '@/components/SearchBar';
import { NotesGrid } from '@/components/NotesGrid';
import { FAB } from '@/components/FAB';
import { CreateNoteSheet } from '@/components/CreateNoteSheet';
import { useFilteredNotes } from '@/hooks/useFilteredNotes';
import { useCreateNoteFlow } from '@/hooks/useCreateNoteFlow';
import { useSettingsStore } from '@/store/useSettingsStore';
import { useState } from 'react';

export function TasksScreen() {
  const [query, setQuery] = useState('');
  const viewMode = useSettingsStore((s) => s.viewMode);
  const notes = useFilteredNotes({ scope: 'active', query, onlyType: 'checklist' });
  const { sheetVisible, openSheet, closeSheet, handleSelect } = useCreateNoteFlow();

  return (
    <ScreenContainer>
      <ScreenHeader title="Tareas" subtitle={`${notes.length} listas`} />
      <View style={styles.searchWrap}>
        <SearchBar value={query} onChangeText={setQuery} placeholder="Buscar tareas..." />
      </View>
      <NotesGrid
        notes={notes}
        viewMode={viewMode}
        mode="active"
        emptyIcon="checkbox-outline"
        emptyTitle="Sin tareas pendientes"
        emptySubtitle="Crea una lista de tareas o checklist con el botón +"
      />
      <FAB onPress={openSheet} />
      <CreateNoteSheet visible={sheetVisible} onClose={closeSheet} onSelect={handleSelect} />
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  searchWrap: { paddingHorizontal: 16, marginBottom: 10 },
});

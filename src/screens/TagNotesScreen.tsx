import React from 'react';
import { RouteProp, useRoute } from '@react-navigation/native';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { NotesGrid } from '@/components/NotesGrid';
import { useFilteredNotes } from '@/hooks/useFilteredNotes';
import { useSettingsStore } from '@/store/useSettingsStore';
import { RootStackParamList } from '@/navigation/types';

export function TagNotesScreen() {
  const route = useRoute<RouteProp<RootStackParamList, 'TagNotes'>>();
  const viewMode = useSettingsStore((s) => s.viewMode);
  const notes = useFilteredNotes({ scope: 'active', tag: route.params.tag });

  return (
    <ScreenContainer>
      <ScreenHeader title={`#${route.params.tag}`} subtitle={`${notes.length} notas`} showBack />
      <NotesGrid
        notes={notes}
        viewMode={viewMode}
        mode="active"
        emptyIcon="pricetag-outline"
        emptyTitle="No hay notas con esta etiqueta"
      />
    </ScreenContainer>
  );
}

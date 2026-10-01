import React from 'react';
import { RouteProp, useRoute } from '@react-navigation/native';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { NotesGrid } from '@/components/NotesGrid';
import { useFilteredNotes } from '@/hooks/useFilteredNotes';
import { useSettingsStore } from '@/store/useSettingsStore';
import { RootStackParamList } from '@/navigation/types';

export function CategoryNotesScreen() {
  const route = useRoute<RouteProp<RootStackParamList, 'CategoryNotes'>>();
  const viewMode = useSettingsStore((s) => s.viewMode);
  const notes = useFilteredNotes({ scope: 'active', categoryId: route.params.categoryId });

  return (
    <ScreenContainer>
      <ScreenHeader title={route.params.categoryName} subtitle={`${notes.length} notas`} showBack />
      <NotesGrid
        notes={notes}
        viewMode={viewMode}
        mode="active"
        emptyIcon="pricetags-outline"
        emptyTitle="No hay notas en esta categoría"
      />
    </ScreenContainer>
  );
}

import React from 'react';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { NotesGrid } from '@/components/NotesGrid';
import { useFilteredNotes } from '@/hooks/useFilteredNotes';
import { useSettingsStore } from '@/store/useSettingsStore';

export function FavoritesScreen() {
  const viewMode = useSettingsStore((s) => s.viewMode);
  const notes = useFilteredNotes({ scope: 'active', onlyFavorites: true });

  return (
    <ScreenContainer>
      <ScreenHeader title="Favoritos" subtitle={`${notes.length} notas`} />
      <NotesGrid
        notes={notes}
        viewMode={viewMode}
        mode="active"
        emptyIcon="heart-outline"
        emptyTitle="Sin favoritas todavía"
        emptySubtitle="Marca notas como favoritas para verlas aquí"
      />
    </ScreenContainer>
  );
}

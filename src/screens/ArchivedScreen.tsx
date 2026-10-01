import React from 'react';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { NotesGrid } from '@/components/NotesGrid';
import { useFilteredNotes } from '@/hooks/useFilteredNotes';
import { useSettingsStore } from '@/store/useSettingsStore';

export function ArchivedScreen() {
  const viewMode = useSettingsStore((s) => s.viewMode);
  const notes = useFilteredNotes({ scope: 'archived' });

  return (
    <ScreenContainer>
      <ScreenHeader title="Archivadas" subtitle={`${notes.length} notas`} />
      <NotesGrid
        notes={notes}
        viewMode={viewMode}
        mode="archived"
        emptyIcon="archive-outline"
        emptyTitle="No hay notas archivadas"
        emptySubtitle="Desliza una nota hacia la izquierda para archivarla"
      />
    </ScreenContainer>
  );
}

import React, { useState } from 'react';
import { FlatList, StyleSheet } from 'react-native';
import * as Sharing from 'expo-sharing';
import * as FileSystem from 'expo-file-system';
import { Note } from '@/types';
import { NoteCard } from '@/components/NoteCard';
import { SwipeableNoteRow } from '@/components/SwipeableNoteRow';
import { SelectionToolbar } from '@/components/SelectionToolbar';
import { EmptyState } from '@/components/EmptyState';
import { useNotesStore } from '@/store/useNotesStore';
import { useOpenNote } from '@/hooks/useOpenNote';
import { stripFormatting } from '@/utils/richText';

export type NotesGridMode = 'active' | 'archived' | 'trash';

interface Props {
  notes: Note[];
  viewMode: 'cards' | 'list';
  mode: NotesGridMode;
  emptyIcon: React.ComponentProps<typeof EmptyState>['icon'];
  emptyTitle: string;
  emptySubtitle?: string;
}

export function NotesGrid({ notes, viewMode, mode, emptyIcon, emptyTitle, emptySubtitle }: Props) {
  const selectedIds = useNotesStore((s) => s.selectedIds);
  const toggleSelect = useNotesStore((s) => s.toggleSelect);
  const clearSelection = useNotesStore((s) => s.clearSelection);
  const togglePinned = useNotesStore((s) => s.togglePinned);
  const toggleFavorite = useNotesStore((s) => s.toggleFavorite);
  const archiveNote = useNotesStore((s) => s.archiveNote);
  const moveToTrash = useNotesStore((s) => s.moveToTrash);
  const restoreFromTrash = useNotesStore((s) => s.restoreFromTrash);
  const deleteForever = useNotesStore((s) => s.deleteForever);
  const openNote = useOpenNote();

  const selectionMode = selectedIds.length > 0;

  const handlePress = (note: Note) => {
    if (selectionMode) {
      toggleSelect(note.id);
      return;
    }
    openNote(note);
  };

  const handleLongPress = (note: Note) => {
    toggleSelect(note.id);
  };

  const shareNote = async (note: Note) => {
    const text = note.type === 'checklist'
      ? note.checklist.map((i) => `${i.checked ? '[x]' : '[ ]'} ${i.text}`).join('\n')
      : stripFormatting(note.content);
    const path = FileSystem.cacheDirectory + `${note.title || 'nota'}.txt`;
    await FileSystem.writeAsStringAsync(path, `${note.title}\n\n${text}`);
    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(path, { mimeType: 'text/plain' });
    }
  };

  if (notes.length === 0) {
    return <EmptyState icon={emptyIcon} title={emptyTitle} subtitle={emptySubtitle} />;
  }

  return (
    <>
      <FlatList
        data={notes}
        key={viewMode}
        keyExtractor={(item) => item.id}
        numColumns={viewMode === 'cards' ? 2 : 1}
        contentContainerStyle={styles.listContent}
        renderItem={({ item }) => {
          const card = (
            <NoteCard
              note={item}
              viewMode={viewMode}
              selected={selectedIds.includes(item.id)}
              selectionMode={selectionMode}
              onPress={() => handlePress(item)}
              onLongPress={() => handleLongPress(item)}
            />
          );
          if (viewMode !== 'list' || selectionMode) return card;
          return (
            <SwipeableNoteRow
              onSwipeArchive={mode === 'active' ? () => archiveNote(item.id, true) : undefined}
              onSwipeDelete={mode === 'trash' ? () => deleteForever(item.id) : () => moveToTrash(item.id)}
              archiveLabel="Archivar"
              deleteLabel={mode === 'trash' ? 'Eliminar' : 'Eliminar'}
            >
              {card}
            </SwipeableNoteRow>
          );
        }}
      />
      {selectionMode && (
        <SelectionToolbar
          count={selectedIds.length}
          onCancel={clearSelection}
          actions={buildActions({
            mode,
            selectedIds,
            notes,
            togglePinned,
            toggleFavorite,
            archiveNote,
            moveToTrash,
            restoreFromTrash,
            deleteForever,
            shareNote,
            clearSelection,
          })}
        />
      )}
    </>
  );
}

function buildActions(ctx: {
  mode: NotesGridMode;
  selectedIds: string[];
  notes: Note[];
  togglePinned: (id: string) => void;
  toggleFavorite: (id: string) => void;
  archiveNote: (id: string, archived: boolean) => void;
  moveToTrash: (id: string) => void;
  restoreFromTrash: (id: string) => void;
  deleteForever: (id: string) => void;
  shareNote: (note: Note) => void;
  clearSelection: () => void;
}) {
  const { mode, selectedIds, notes, clearSelection } = ctx;
  const run = (fn: (id: string) => void) => {
    selectedIds.forEach(fn);
    clearSelection();
  };

  if (mode === 'trash') {
    return [
      { key: 'restore', icon: 'refresh' as const, label: 'Restaurar', onPress: () => run(ctx.restoreFromTrash) },
      {
        key: 'delete',
        icon: 'trash' as const,
        label: 'Eliminar',
        danger: true,
        onPress: () => run(ctx.deleteForever),
      },
    ];
  }

  if (mode === 'archived') {
    return [
      { key: 'unarchive', icon: 'archive-outline' as const, label: 'Desarchivar', onPress: () => run((id) => ctx.archiveNote(id, false)) },
      { key: 'trash', icon: 'trash' as const, label: 'Eliminar', danger: true, onPress: () => run(ctx.moveToTrash) },
    ];
  }

  return [
    { key: 'pin', icon: 'pin' as const, label: 'Fijar', onPress: () => run(ctx.togglePinned) },
    { key: 'favorite', icon: 'heart' as const, label: 'Favorito', onPress: () => run(ctx.toggleFavorite) },
    {
      key: 'share',
      icon: 'share-social' as const,
      label: 'Compartir',
      onPress: () => {
        selectedIds.forEach((id) => {
          const note = notes.find((n) => n.id === id);
          if (note) ctx.shareNote(note);
        });
        clearSelection();
      },
    },
    { key: 'archive', icon: 'archive' as const, label: 'Archivar', onPress: () => run((id) => ctx.archiveNote(id, true)) },
    { key: 'trash', icon: 'trash' as const, label: 'Eliminar', danger: true, onPress: () => run(ctx.moveToTrash) },
  ];
}

const styles = StyleSheet.create({
  listContent: {
    paddingHorizontal: 10,
    paddingBottom: 100,
  },
});

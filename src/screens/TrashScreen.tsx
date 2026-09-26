import React from 'react';
import { Alert, Pressable, StyleSheet, Text } from 'react-native';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { NotesGrid } from '@/components/NotesGrid';
import { useFilteredNotes } from '@/hooks/useFilteredNotes';
import { useSettingsStore } from '@/store/useSettingsStore';
import { useNotesStore } from '@/store/useNotesStore';
import { useAppTheme } from '@/theme/ThemeContext';

export function TrashScreen() {
  const { colors, fontSizes } = useAppTheme();
  const viewMode = useSettingsStore((s) => s.viewMode);
  const notes = useFilteredNotes({ scope: 'trash' });
  const emptyTrash = useNotesStore((s) => s.emptyTrash);

  const confirmEmpty = () => {
    Alert.alert(
      'Vaciar papelera',
      'Esta acción eliminará permanentemente todas las notas en la papelera.',
      [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Vaciar', style: 'destructive', onPress: () => emptyTrash() },
      ]
    );
  };

  return (
    <ScreenContainer>
      <ScreenHeader
        title="Papelera"
        subtitle={`${notes.length} notas · se eliminan automáticamente en 30 días`}
        right={
          notes.length > 0 ? (
            <Pressable onPress={confirmEmpty} style={styles.emptyButton}>
              <Text style={{ color: colors.danger, fontSize: fontSizes.sm, fontWeight: '700' }}>Vaciar</Text>
            </Pressable>
          ) : undefined
        }
      />
      <NotesGrid
        notes={notes}
        viewMode={viewMode}
        mode="trash"
        emptyIcon="trash-outline"
        emptyTitle="La papelera está vacía"
        emptySubtitle="Las notas eliminadas aparecerán aquí"
      />
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  emptyButton: {
    paddingHorizontal: 8,
    paddingVertical: 6,
  },
});

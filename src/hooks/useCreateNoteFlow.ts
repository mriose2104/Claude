import { useState } from 'react';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNotesStore } from '@/store/useNotesStore';
import { NoteType } from '@/types';
import { RootStackParamList } from '@/navigation/types';

export function useCreateNoteFlow(defaultCategoryId: string | null = null) {
  const [sheetVisible, setSheetVisible] = useState(false);
  const createNote = useNotesStore((s) => s.createNote);
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

  const openSheet = () => setSheetVisible(true);
  const closeSheet = () => setSheetVisible(false);

  const handleSelect = async (type: NoteType, quick: boolean) => {
    setSheetVisible(false);
    const note = await createNote(type, defaultCategoryId);
    navigation.navigate('NoteEditor', { noteId: note.id });
    void quick;
  };

  return { sheetVisible, openSheet, closeSheet, handleSelect };
}

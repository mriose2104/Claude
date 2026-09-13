import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Note } from '@/types';
import { RootStackParamList } from '@/navigation/types';
import { useNoteUnlock } from '@/security/NoteUnlockContext';

export function useOpenNote() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const requestUnlock = useNoteUnlock();

  return async function openNote(note: Note) {
    if (note.locked) {
      const ok = await requestUnlock();
      if (!ok) return;
    }
    navigation.navigate('NoteEditor', { noteId: note.id });
  };
}

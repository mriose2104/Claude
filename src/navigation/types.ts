import { NoteType } from '@/types';

export type RootStackParamList = {
  Main: undefined;
  NoteEditor: { noteId: string; searchQuery?: string };
  CategoryNotes: { categoryId: string; categoryName: string };
  TagNotes: { tag: string };
  CreateNoteSheet: { defaultType?: NoteType } | undefined;
};

export type DrawerParamList = {
  Inicio: undefined;
  Notas: undefined;
  Tareas: undefined;
  Favoritos: undefined;
  Categorias: undefined;
  Archivadas: undefined;
  Papelera: undefined;
  Configuracion: undefined;
};

import React from 'react';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { DrawerParamList } from '@/navigation/types';
import { DrawerContent } from '@/navigation/DrawerContent';
import { HomeScreen } from '@/screens/HomeScreen';
import { NotesScreen } from '@/screens/NotesScreen';
import { TasksScreen } from '@/screens/TasksScreen';
import { FavoritesScreen } from '@/screens/FavoritesScreen';
import { CategoriesScreen } from '@/screens/CategoriesScreen';
import { ArchivedScreen } from '@/screens/ArchivedScreen';
import { TrashScreen } from '@/screens/TrashScreen';
import { SettingsScreen } from '@/screens/SettingsScreen';
import { useAppTheme } from '@/theme/ThemeContext';

const Drawer = createDrawerNavigator<DrawerParamList>();

export function MainDrawer() {
  const { colors } = useAppTheme();
  return (
    <Drawer.Navigator
      drawerContent={(props) => <DrawerContent {...props} />}
      screenOptions={{
        headerShown: false,
        drawerType: 'front',
        overlayColor: colors.overlay,
        sceneContainerStyle: { backgroundColor: colors.background },
      }}
    >
      <Drawer.Screen name="Inicio" component={HomeScreen} />
      <Drawer.Screen name="Notas" component={NotesScreen} />
      <Drawer.Screen name="Tareas" component={TasksScreen} />
      <Drawer.Screen name="Favoritos" component={FavoritesScreen} />
      <Drawer.Screen name="Categorias" component={CategoriesScreen} />
      <Drawer.Screen name="Archivadas" component={ArchivedScreen} />
      <Drawer.Screen name="Papelera" component={TrashScreen} />
      <Drawer.Screen name="Configuracion" component={SettingsScreen} />
    </Drawer.Navigator>
  );
}

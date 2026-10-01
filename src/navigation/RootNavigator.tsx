import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { RootStackParamList } from '@/navigation/types';
import { MainDrawer } from '@/navigation/MainDrawer';
import { NoteEditorScreen } from '@/screens/NoteEditorScreen';
import { CategoryNotesScreen } from '@/screens/CategoryNotesScreen';
import { TagNotesScreen } from '@/screens/TagNotesScreen';

const Stack = createNativeStackNavigator<RootStackParamList>();

export function RootNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Main" component={MainDrawer} />
      <Stack.Screen
        name="NoteEditor"
        component={NoteEditorScreen}
        options={{ presentation: 'modal', animation: 'slide_from_bottom' }}
      />
      <Stack.Screen name="CategoryNotes" component={CategoryNotesScreen} />
      <Stack.Screen name="TagNotes" component={TagNotesScreen} />
    </Stack.Navigator>
  );
}

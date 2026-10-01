import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { DrawerContentComponentProps, DrawerContentScrollView, DrawerItem } from '@react-navigation/drawer';
import { Ionicons } from '@expo/vector-icons';
import { useAppTheme } from '@/theme/ThemeContext';
import { useNotesStore } from '@/store/useNotesStore';

const ITEMS: { route: string; label: string; icon: keyof typeof Ionicons.glyphMap }[] = [
  { route: 'Inicio', label: 'Inicio', icon: 'home' },
  { route: 'Notas', label: 'Notas', icon: 'document-text' },
  { route: 'Tareas', label: 'Tareas', icon: 'checkbox' },
  { route: 'Favoritos', label: 'Favoritos', icon: 'heart' },
  { route: 'Categorias', label: 'Categorías', icon: 'pricetags' },
  { route: 'Archivadas', label: 'Archivadas', icon: 'archive' },
  { route: 'Papelera', label: 'Papelera', icon: 'trash' },
  { route: 'Configuracion', label: 'Configuración', icon: 'settings' },
];

export function DrawerContent(props: DrawerContentComponentProps) {
  const { colors, fontSizes } = useAppTheme();
  const activeRoute = props.state.routeNames[props.state.index];
  const notes = useNotesStore((s) => s.notes);
  const activeCount = notes.filter((n) => !n.archived && !n.deleted).length;

  return (
    <DrawerContentScrollView {...props} contentContainerStyle={{ backgroundColor: colors.surface, flex: 1 }}>
      <View style={styles.header}>
        <View style={[styles.logo, { backgroundColor: colors.primary }]}>
          <Ionicons name="albums" size={26} color="#fff" />
        </View>
        <Text style={[styles.appName, { color: colors.text, fontSize: fontSizes.lg }]}>Notes Pro</Text>
        <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs }}>{activeCount} notas activas</Text>
      </View>

      {ITEMS.map((item) => {
        const focused = activeRoute === item.route;
        return (
          <DrawerItem
            key={item.route}
            label={item.label}
            focused={focused}
            activeBackgroundColor={colors.chipBackground}
            activeTintColor={colors.primary}
            inactiveTintColor={colors.textSecondary}
            labelStyle={{ fontSize: fontSizes.sm, fontWeight: focused ? '700' : '500' }}
            icon={({ color, size }) => <Ionicons name={item.icon} size={size} color={color} />}
            onPress={() => props.navigation.navigate(item.route)}
          />
        );
      })}
    </DrawerContentScrollView>
  );
}

const styles = StyleSheet.create({
  header: {
    paddingHorizontal: 20,
    paddingTop: 24,
    paddingBottom: 20,
  },
  logo: {
    width: 48,
    height: 48,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 10,
  },
  appName: {
    fontWeight: '800',
  },
});

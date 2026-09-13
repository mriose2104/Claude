import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { DrawerNavigationProp } from '@react-navigation/drawer';
import { Ionicons } from '@expo/vector-icons';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  title: string;
  subtitle?: string;
  right?: React.ReactNode;
  showBack?: boolean;
}

export function ScreenHeader({ title, subtitle, right, showBack }: Props) {
  const { colors, fontSizes } = useAppTheme();
  const navigation = useNavigation<DrawerNavigationProp<any>>();

  return (
    <View style={styles.container}>
      <View style={styles.left}>
        <Pressable
          hitSlop={10}
          style={[styles.menuButton, { backgroundColor: colors.chipBackground }]}
          onPress={() => (showBack ? navigation.goBack() : navigation.openDrawer())}
        >
          <Ionicons name={showBack ? 'arrow-back' : 'menu'} size={22} color={colors.text} />
        </Pressable>
        <View>
          <Text style={[styles.title, { color: colors.text, fontSize: fontSizes.xl }]}>{title}</Text>
          {subtitle && (
            <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs }}>{subtitle}</Text>
          )}
        </View>
      </View>
      {right && <View style={styles.right}>{right}</View>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 12,
  },
  left: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  menuButton: {
    width: 40,
    height: 40,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  title: {
    fontWeight: '800',
  },
  right: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
});

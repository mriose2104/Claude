import React, { useRef } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Swipeable } from 'react-native-gesture-handler';
import { Ionicons } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  children: React.ReactNode;
  onSwipeArchive?: () => void;
  onSwipeDelete?: () => void;
  archiveLabel?: string;
  deleteLabel?: string;
  disabled?: boolean;
}

export function SwipeableNoteRow({
  children,
  onSwipeArchive,
  onSwipeDelete,
  archiveLabel = 'Archivar',
  deleteLabel = 'Eliminar',
  disabled,
}: Props) {
  const { colors } = useAppTheme();
  const ref = useRef<Swipeable>(null);

  if (disabled) return <>{children}</>;

  return (
    <Swipeable
      ref={ref}
      overshootLeft={false}
      overshootRight={false}
      renderLeftActions={
        onSwipeArchive
          ? () => (
              <View style={[styles.action, { backgroundColor: colors.primary }]}>
                <Ionicons name="archive" size={22} color="#fff" />
                <Text style={styles.actionText}>{archiveLabel}</Text>
              </View>
            )
          : undefined
      }
      renderRightActions={
        onSwipeDelete
          ? () => (
              <View style={[styles.action, { backgroundColor: colors.danger }]}>
                <Ionicons name="trash" size={22} color="#fff" />
                <Text style={styles.actionText}>{deleteLabel}</Text>
              </View>
            )
          : undefined
      }
      onSwipeableOpen={(direction) => {
        Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
        ref.current?.close();
        if (direction === 'left') onSwipeArchive?.();
        if (direction === 'right') onSwipeDelete?.();
      }}
    >
      {children}
    </Swipeable>
  );
}

const styles = StyleSheet.create({
  action: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 18,
    marginBottom: 12,
  },
  actionText: {
    color: '#fff',
    fontWeight: '700',
    marginTop: 4,
    fontSize: 12,
  },
});

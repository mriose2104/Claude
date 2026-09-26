import React, { useState } from 'react';
import { Alert, Modal, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { ScreenContainer } from '@/components/ScreenContainer';
import { ScreenHeader } from '@/components/ScreenHeader';
import { SettingsSection } from '@/components/SettingsSection';
import { SettingsRow } from '@/components/SettingsRow';
import { Chip } from '@/components/Chip';
import { PinSetupModal } from '@/components/PinSetupModal';
import { useAppTheme } from '@/theme/ThemeContext';
import { useSettingsStore } from '@/store/useSettingsStore';
import { useSecurityStore } from '@/store/useSecurityStore';
import { SortBy, TextSize, ThemeMode, ViewMode } from '@/types';
import { isBiometricAvailable } from '@/utils/security';
import {
  createManualBackup,
  exportNotesToFile,
  hasLocalBackup,
  importNotesFromFile,
  restoreFromLocalBackup,
} from '@/db/backupRepository';
import { useNotesStore } from '@/store/useNotesStore';

const THEME_OPTIONS: { value: ThemeMode; label: string }[] = [
  { value: 'light', label: 'Claro' },
  { value: 'dark', label: 'Oscuro' },
  { value: 'system', label: 'Automático' },
];

const TEXT_SIZE_OPTIONS: { value: TextSize; label: string }[] = [
  { value: 'small', label: 'Pequeño' },
  { value: 'medium', label: 'Medio' },
  { value: 'large', label: 'Grande' },
];

const VIEW_OPTIONS: { value: ViewMode; label: string }[] = [
  { value: 'cards', label: 'Tarjetas' },
  { value: 'list', label: 'Lista' },
];

const SORT_OPTIONS: { value: SortBy; label: string }[] = [
  { value: 'updatedAt', label: 'Modificación' },
  { value: 'createdAt', label: 'Creación' },
  { value: 'title', label: 'Nombre' },
  { value: 'category', label: 'Categoría' },
];

export function SettingsScreen() {
  const { colors, fontSizes } = useAppTheme();
  const settings = useSettingsStore();
  const security = useSecurityStore();
  const loadAll = useNotesStore((s) => s.loadAll);

  const [pinModalVisible, setPinModalVisible] = useState(false);
  const [aboutVisible, setAboutVisible] = useState(false);
  const [busy, setBusy] = useState(false);

  const handlePinConfirm = async (pin: string) => {
    await security.setPin(pin);
    setPinModalVisible(false);
    Alert.alert('PIN configurado', 'Tu PIN se guardó correctamente.');
  };

  const handleTogglePin = (value: boolean) => {
    if (value) {
      setPinModalVisible(true);
    } else {
      Alert.alert('Desactivar PIN', '¿Seguro que quieres desactivar el bloqueo con PIN?', [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Desactivar', style: 'destructive', onPress: () => security.disablePin() },
      ]);
    }
  };

  const handleToggleBiometric = async (value: boolean) => {
    if (!security.pinEnabled) {
      Alert.alert('Configura un PIN primero', 'La huella digital funciona junto con un PIN de respaldo.');
      return;
    }
    if (value) {
      const available = await isBiometricAvailable();
      if (!available) {
        Alert.alert('No disponible', 'Este dispositivo no tiene biometría configurada.');
        return;
      }
    }
    await security.setBiometricEnabled(value);
  };

  const handleManualBackup = async () => {
    setBusy(true);
    try {
      const result = await createManualBackup();
      Alert.alert(
        'Copia de seguridad creada',
        result.savedToDownloads
          ? 'Tus notas se guardaron en la app y también en una carpeta que elegiste (por ejemplo, Descargas).'
          : 'Tus notas se guardaron dentro de la app. No se pudo guardar una copia adicional visible: puedes intentarlo de nuevo o usar "Exportar notas".'
      );
    } catch (e) {
      Alert.alert('Error', 'No se pudo crear la copia de seguridad.');
    } finally {
      setBusy(false);
    }
  };

  const handleRestore = async () => {
    const exists = await hasLocalBackup();
    if (!exists) {
      Alert.alert('Sin copia disponible', 'Primero crea una copia de seguridad manual.');
      return;
    }
    Alert.alert('Restaurar copia', 'Esto reemplazará tus notas actuales con la última copia guardada.', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Restaurar',
        style: 'destructive',
        onPress: async () => {
          setBusy(true);
          try {
            const count = await restoreFromLocalBackup();
            await loadAll();
            Alert.alert('Restauración completa', `${count} notas restauradas.`);
          } catch {
            Alert.alert('Error', 'No se pudo restaurar la copia de seguridad.');
          } finally {
            setBusy(false);
          }
        },
      },
    ]);
  };

  const handleExport = async () => {
    setBusy(true);
    try {
      await exportNotesToFile();
    } catch {
      Alert.alert('Error', 'No se pudo exportar las notas.');
    } finally {
      setBusy(false);
    }
  };

  const handleImport = async (mode: 'merge' | 'replace') => {
    setBusy(true);
    try {
      const count = await importNotesFromFile(mode);
      if (count > 0) {
        await loadAll();
        Alert.alert('Importación completa', `${count} notas importadas.`);
      }
    } catch {
      Alert.alert('Error', 'El archivo no pudo importarse. Verifica que sea un backup válido.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <ScreenContainer>
      <ScreenHeader title="Configuración" />
      <ScrollView contentContainerStyle={styles.scroll}>
        <SettingsSection title="Apariencia">
          <View style={styles.chipRow}>
            {THEME_OPTIONS.map((opt) => (
              <Chip key={opt.value} label={opt.label} active={settings.themeMode === opt.value} onPress={() => settings.setThemeMode(opt.value)} />
            ))}
          </View>
          <Divider />
          <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs, marginBottom: 8 }}>Tamaño de texto</Text>
          <View style={styles.chipRow}>
            {TEXT_SIZE_OPTIONS.map((opt) => (
              <Chip key={opt.value} label={opt.label} active={settings.textSize === opt.value} onPress={() => settings.setTextSize(opt.value)} />
            ))}
          </View>
        </SettingsSection>

        <SettingsSection title="Notas">
          <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs, marginBottom: 8, marginTop: 4 }}>
            Vista predeterminada
          </Text>
          <View style={styles.chipRow}>
            {VIEW_OPTIONS.map((opt) => (
              <Chip key={opt.value} label={opt.label} active={settings.viewMode === opt.value} onPress={() => settings.setViewMode(opt.value)} />
            ))}
          </View>
          <Divider />
          <Text style={{ color: colors.textMuted, fontSize: fontSizes.xs, marginBottom: 8 }}>Ordenar por</Text>
          <View style={styles.chipRow}>
            {SORT_OPTIONS.map((opt) => (
              <Chip key={opt.value} label={opt.label} active={settings.sortBy === opt.value} onPress={() => settings.setSortBy(opt.value)} />
            ))}
          </View>
        </SettingsSection>

        <SettingsSection title="Recordatorios">
          <SettingsRow
            icon="volume-high-outline"
            label="Sonido de recordatorios"
            switchValue={settings.reminderSoundEnabled}
            onSwitchChange={settings.setReminderSoundEnabled}
          />
        </SettingsSection>

        <SettingsSection title="Seguridad">
          <SettingsRow icon="keypad-outline" label="Bloqueo con PIN" switchValue={security.pinEnabled} onSwitchChange={handleTogglePin} />
          <SettingsRow
            icon="finger-print-outline"
            label="Huella digital / biometría"
            switchValue={security.biometricEnabled}
            onSwitchChange={handleToggleBiometric}
          />
          <SettingsRow
            icon="lock-closed-outline"
            label="Bloquear al salir de la app"
            switchValue={security.lockOnExit}
            onSwitchChange={security.setLockOnExit}
          />
          {security.pinEnabled && (
            <SettingsRow icon="key-outline" label="Cambiar PIN" onPress={() => setPinModalVisible(true)} />
          )}
        </SettingsSection>

        <SettingsSection title="Respaldo">
          <SettingsRow icon="save-outline" label="Copia de seguridad manual" onPress={handleManualBackup} />
          <SettingsRow icon="refresh-outline" label="Restaurar copia local" onPress={handleRestore} />
          <SettingsRow icon="download-outline" label="Exportar notas (.json)" onPress={handleExport} />
          <SettingsRow icon="cloud-upload-outline" label="Importar y combinar" onPress={() => handleImport('merge')} />
          <SettingsRow icon="cloud-upload" label="Importar y reemplazar" onPress={() => handleImport('replace')} danger />
        </SettingsSection>

        <SettingsSection title="Acerca de">
          <SettingsRow icon="information-circle-outline" label="Acerca de Notes Pro" onPress={() => setAboutVisible(true)} />
        </SettingsSection>
      </ScrollView>

      <PinSetupModal visible={pinModalVisible} onConfirm={handlePinConfirm} onClose={() => setPinModalVisible(false)} />

      <Modal visible={aboutVisible} transparent animationType="fade" onRequestClose={() => setAboutVisible(false)}>
        <Pressable style={[styles.backdrop, { backgroundColor: colors.overlay }]} onPress={() => setAboutVisible(false)}>
          <Pressable style={[styles.aboutCard, { backgroundColor: colors.surface }]} onPress={(e) => e.stopPropagation()}>
            <Text style={{ color: colors.text, fontSize: fontSizes.lg, fontWeight: '800', marginBottom: 6 }}>Notes Pro</Text>
            <Text style={{ color: colors.textMuted, fontSize: fontSizes.sm, marginBottom: 12 }}>Versión 1.0.0</Text>
            <Text style={{ color: colors.textSecondary, fontSize: fontSizes.sm, lineHeight: 20 }}>
              Una aplicación de notas profesional, rápida y funcional sin conexión, con recordatorios,
              organización por categorías y etiquetas, y protección con PIN y biometría.
            </Text>
          </Pressable>
        </Pressable>
      </Modal>
    </ScreenContainer>
  );
}

function Divider() {
  const { colors } = useAppTheme();
  return <View style={{ height: StyleSheet.hairlineWidth, backgroundColor: colors.border, marginVertical: 12 }} />;
}

const styles = StyleSheet.create({
  scroll: {
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 60,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  backdrop: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  aboutCard: {
    width: '86%',
    borderRadius: 20,
    padding: 24,
  },
});

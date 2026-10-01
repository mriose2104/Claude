import React, { useState } from 'react';
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { PinPad } from '@/components/PinPad';
import { useAppTheme } from '@/theme/ThemeContext';

interface Props {
  visible: boolean;
  onConfirm: (pin: string) => void;
  onClose: () => void;
}

export function PinSetupModal({ visible, onConfirm, onClose }: Props) {
  const { colors, fontSizes } = useAppTheme();
  const [stage, setStage] = useState<'enter' | 'confirm'>('enter');
  const [firstPin, setFirstPin] = useState('');
  const [pin, setPin] = useState('');
  const [error, setError] = useState(false);

  const reset = () => {
    setStage('enter');
    setFirstPin('');
    setPin('');
    setError(false);
  };

  const submitStage = (value: string) => {
    if (stage === 'enter') {
      if (value.length < 4) return;
      setFirstPin(value);
      setPin('');
      setStage('confirm');
    } else {
      if (value !== firstPin) {
        setError(true);
        setTimeout(() => {
          setPin('');
          setError(false);
        }, 500);
        return;
      }
      onConfirm(value);
      reset();
    }
  };

  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={() => {
        reset();
        onClose();
      }}
    >
      <View style={[styles.backdrop, { backgroundColor: colors.overlay }]}>
        <View style={[styles.card, { backgroundColor: colors.surface }]}>
          <Text style={{ color: colors.text, fontSize: fontSizes.lg, fontWeight: '700', marginBottom: 4 }}>
            {stage === 'enter' ? 'Crea un PIN' : 'Confirma tu PIN'}
          </Text>
          <Text style={{ color: colors.textMuted, fontSize: fontSizes.sm, marginBottom: 20 }}>
            {stage === 'enter' ? 'Usa entre 4 y 6 dígitos' : 'Ingresa el mismo PIN nuevamente'}
          </Text>
          <PinPad
            length={Math.max(4, pin.length)}
            value={pin}
            error={error}
            onDigit={(d) => {
              const next = (pin + d).slice(0, 6);
              setPin(next);
              setError(false);
              if (next.length === 6) submitStage(next);
            }}
            onBackspace={() => setPin((p) => p.slice(0, -1))}
          />
          <View style={styles.actions}>
            <Pressable
              onPress={() => {
                reset();
                onClose();
              }}
              style={styles.actionButton}
            >
              <Text style={{ color: colors.textMuted, fontSize: fontSizes.sm }}>Cancelar</Text>
            </Pressable>
            <Pressable
              onPress={() => submitStage(pin)}
              style={styles.actionButton}
              disabled={pin.length < 4}
            >
              <Text style={{ color: pin.length < 4 ? colors.textMuted : colors.primary, fontSize: fontSizes.sm, fontWeight: '700' }}>
                {stage === 'enter' ? 'Continuar' : 'Confirmar'}
              </Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  card: {
    width: '86%',
    borderRadius: 24,
    padding: 24,
    alignItems: 'center',
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
    marginTop: 20,
  },
  actionButton: {
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
});

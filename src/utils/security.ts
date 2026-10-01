import * as Crypto from 'expo-crypto';
import * as LocalAuthentication from 'expo-local-authentication';

export async function hashPin(pin: string): Promise<string> {
  return Crypto.digestStringAsync(Crypto.CryptoDigestAlgorithm.SHA256, pin);
}

export async function verifyPin(pin: string, hash: string): Promise<boolean> {
  const candidate = await hashPin(pin);
  return candidate === hash;
}

export async function isBiometricAvailable(): Promise<boolean> {
  const hasHardware = await LocalAuthentication.hasHardwareAsync();
  const isEnrolled = await LocalAuthentication.isEnrolledAsync();
  return hasHardware && isEnrolled;
}

export async function authenticateWithBiometrics(promptMessage: string): Promise<boolean> {
  const available = await isBiometricAvailable();
  if (!available) return false;
  const result = await LocalAuthentication.authenticateAsync({
    promptMessage,
    cancelLabel: 'Cancelar',
    disableDeviceFallback: false,
  });
  return result.success;
}

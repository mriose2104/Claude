import { create } from 'zustand';
import * as SecureStore from 'expo-secure-store';
import { hashPin } from '@/utils/security';

const PIN_HASH_KEY = 'notes_pro_pin_hash';
const PIN_ENABLED_KEY = 'notes_pro_pin_enabled';
const BIOMETRIC_ENABLED_KEY = 'notes_pro_biometric_enabled';
const LOCK_ON_EXIT_KEY = 'notes_pro_lock_on_exit';

interface SecurityState {
  hydrated: boolean;
  pinEnabled: boolean;
  pinHash: string | null;
  biometricEnabled: boolean;
  lockOnExit: boolean;
  /** True while the app should show the lock screen. */
  isLocked: boolean;
  hydrate: () => Promise<void>;
  setPin: (pin: string) => Promise<void>;
  disablePin: () => Promise<void>;
  setBiometricEnabled: (enabled: boolean) => Promise<void>;
  setLockOnExit: (enabled: boolean) => Promise<void>;
  lock: () => void;
  unlock: () => void;
}

export const useSecurityStore = create<SecurityState>((set, get) => ({
  hydrated: false,
  pinEnabled: false,
  pinHash: null,
  biometricEnabled: false,
  lockOnExit: true,
  isLocked: false,

  hydrate: async () => {
    const [pinHash, pinEnabled, biometricEnabled, lockOnExit] = await Promise.all([
      SecureStore.getItemAsync(PIN_HASH_KEY),
      SecureStore.getItemAsync(PIN_ENABLED_KEY),
      SecureStore.getItemAsync(BIOMETRIC_ENABLED_KEY),
      SecureStore.getItemAsync(LOCK_ON_EXIT_KEY),
    ]);
    const enabled = pinEnabled === 'true';
    set({
      pinHash,
      pinEnabled: enabled,
      biometricEnabled: biometricEnabled === 'true',
      lockOnExit: lockOnExit !== 'false',
      isLocked: enabled,
      hydrated: true,
    });
  },

  setPin: async (pin) => {
    const hash = await hashPin(pin);
    await SecureStore.setItemAsync(PIN_HASH_KEY, hash);
    await SecureStore.setItemAsync(PIN_ENABLED_KEY, 'true');
    set({ pinHash: hash, pinEnabled: true });
  },

  disablePin: async () => {
    await SecureStore.deleteItemAsync(PIN_HASH_KEY);
    await SecureStore.setItemAsync(PIN_ENABLED_KEY, 'false');
    await SecureStore.setItemAsync(BIOMETRIC_ENABLED_KEY, 'false');
    set({ pinHash: null, pinEnabled: false, biometricEnabled: false });
  },

  setBiometricEnabled: async (enabled) => {
    await SecureStore.setItemAsync(BIOMETRIC_ENABLED_KEY, String(enabled));
    set({ biometricEnabled: enabled });
  },

  setLockOnExit: async (enabled) => {
    await SecureStore.setItemAsync(LOCK_ON_EXIT_KEY, String(enabled));
    set({ lockOnExit: enabled });
  },

  lock: () => {
    if (get().pinEnabled) set({ isLocked: true });
  },
  unlock: () => set({ isLocked: false }),
}));

import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';
import { Note, ReminderRepeat } from '@/types';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

export async function ensureNotificationPermission(): Promise<boolean> {
  const current = await Notifications.getPermissionsAsync();
  if (current.granted) return true;
  const requested = await Notifications.requestPermissionsAsync();
  return requested.granted;
}

/**
 * expo-notifications (SDK 51) has no native monthly-repeat primitive, so monthly reminders
 * are scheduled as a single date trigger and advanced to the next month by
 * `advanceOverdueMonthlyReminder` once they are due (see useNotesStore.loadAll).
 */
function repeatToTrigger(
  date: Date,
  repeat: ReminderRepeat
): Notifications.NotificationTriggerInput {
  if (repeat === 'daily') {
    return {
      hour: date.getHours(),
      minute: date.getMinutes(),
      repeats: true,
    };
  }
  if (repeat === 'weekly') {
    return {
      weekday: date.getDay() + 1,
      hour: date.getHours(),
      minute: date.getMinutes(),
      repeats: true,
    };
  }
  return date;
}

/** For a monthly reminder that already fired, returns the same day/time next month. */
export function nextMonthlyOccurrence(timestamp: number): number {
  const date = new Date(timestamp);
  const next = new Date(date);
  next.setMonth(next.getMonth() + 1);
  const now = Date.now();
  while (next.getTime() <= now) {
    next.setMonth(next.getMonth() + 1);
  }
  return next.getTime();
}

/** Schedules a local reminder for a note. Locked notes never leak their content in the notification body. */
export async function scheduleNoteReminder(note: Note): Promise<string | null> {
  if (!note.reminderAt) return null;
  const granted = await ensureNotificationPermission();
  if (!granted) return null;

  const body = note.locked
    ? 'Tienes una nota protegida pendiente.'
    : (note.content || plainChecklistPreview(note)).slice(0, 120) || 'Recordatorio de nota';

  const id = await Notifications.scheduleNotificationAsync({
    content: {
      title: note.locked ? 'Nota protegida' : note.title || 'Recordatorio',
      body,
      data: { noteId: note.id },
      sound: Platform.OS === 'android' ? undefined : true,
    },
    trigger: repeatToTrigger(new Date(note.reminderAt), note.reminderRepeat),
  });
  return id;
}

export async function cancelNoteReminder(notificationId: string | null): Promise<void> {
  if (!notificationId) return;
  try {
    await Notifications.cancelScheduledNotificationAsync(notificationId);
  } catch {
    // Already fired or cancelled; nothing to do.
  }
}

function plainChecklistPreview(note: Note): string {
  return note.checklist.map((item) => item.text).join(', ');
}

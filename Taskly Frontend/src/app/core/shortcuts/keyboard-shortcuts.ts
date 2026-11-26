/**
 * Keyboard shortcuts for the workspace, kept free of Angular so the rules are easy to test.
 * Single keys (N, /, ?) act at once; "G then a letter" jumps to a view, like Gmail or GitHub.
 */

export type WorkspaceTab = 'DASHBOARD' | 'MY_TASKS' | 'BOARDS' | 'CALENDAR' | 'TEAM' | 'REPORTS' | 'SETTINGS';

export type ShortcutAction =
  | { type: 'goto'; tab: WorkspaceTab }
  | { type: 'newTask' }
  | { type: 'search' }
  | { type: 'help' }
  | { type: 'escape' };

export interface ShortcutInfo {
  keys: string[];
  description: string;
}

export interface ShortcutGroup {
  title: string;
  shortcuts: ShortcutInfo[];
}

const GOTO_KEYS: Record<string, { tab: WorkspaceTab; label: string }> = {
  d: { tab: 'DASHBOARD', label: 'Dashboard' },
  m: { tab: 'MY_TASKS', label: 'My Tasks' },
  b: { tab: 'BOARDS', label: 'Boards' },
  c: { tab: 'CALENDAR', label: 'Calendar' },
  t: { tab: 'TEAM', label: 'Team' },
  r: { tab: 'REPORTS', label: 'Reports' },
  s: { tab: 'SETTINGS', label: 'Settings' },
};

/** What the "?" overlay lists, generated from the same table the resolver uses. */
export const SHORTCUT_GROUPS: ShortcutGroup[] = [
  {
    title: 'Actions',
    shortcuts: [
      { keys: ['N'], description: 'New task in the open board' },
      { keys: ['/'], description: 'Search tasks' },
      { keys: ['?'], description: 'Show or hide this list' },
      { keys: ['Esc'], description: 'Close any open window' },
    ],
  },
  {
    title: 'Go to',
    shortcuts: Object.entries(GOTO_KEYS).map(([key, { label }]) => ({
      keys: ['G', key.toUpperCase()],
      description: label,
    })),
  },
];

/** How long after pressing G the second key still counts */
export const SEQUENCE_TIMEOUT_MS = 1000;

/** True when the key press is meant for a text field, so shortcuts must stay out of the way. */
export function isTypingTarget(target: EventTarget | null): boolean {
  if (!(target instanceof HTMLElement)) return false;
  const tag = target.tagName;
  return tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || target.isContentEditable;
}

export class ShortcutResolver {
  private pendingGAt: number | null = null;

  /** Turns a key press into an action, or null when the key isn't a shortcut. */
  resolve(event: Pick<KeyboardEvent, 'key' | 'target' | 'ctrlKey' | 'metaKey' | 'altKey'>,
          now: number = Date.now()): ShortcutAction | null {
    // Escape always works, even inside a text field, so you can back out of anything
    if (event.key === 'Escape') {
      this.pendingGAt = null;
      return { type: 'escape' };
    }

    // Leave browser and OS shortcuts (Ctrl+C, Cmd+R...) and typing alone
    if (event.ctrlKey || event.metaKey || event.altKey || isTypingTarget(event.target)) {
      this.pendingGAt = null;
      return null;
    }

    const key = event.key.length === 1 ? event.key.toLowerCase() : event.key;

    if (this.pendingGAt !== null) {
      const withinTime = now - this.pendingGAt <= SEQUENCE_TIMEOUT_MS;
      this.pendingGAt = null;
      const target = GOTO_KEYS[key];
      if (withinTime && target) {
        return { type: 'goto', tab: target.tab };
      }
      // Too slow or not a view letter: treat this press as a fresh one below
    }

    switch (key) {
      case 'g':
        this.pendingGAt = now;
        return null;
      case 'n':
        return { type: 'newTask' };
      case '/':
        return { type: 'search' };
      case '?':
        return { type: 'help' };
      default:
        return null;
    }
  }
}

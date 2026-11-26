import { TestBed } from '@angular/core/testing';
import { SEQUENCE_TIMEOUT_MS, ShortcutResolver } from './keyboard-shortcuts';
import { ShortcutHelpComponent } from '../../shared/shortcut-help/shortcut-help';

function key(k: string, extra: Partial<KeyboardEvent> = {}) {
  return { key: k, target: document.body, ctrlKey: false, metaKey: false, altKey: false, ...extra };
}

describe('ShortcutResolver', () => {
  let resolver: ShortcutResolver;

  beforeEach(() => (resolver = new ShortcutResolver()));

  it('maps single keys to actions', () => {
    expect(resolver.resolve(key('n'))).toEqual({ type: 'newTask' });
    expect(resolver.resolve(key('N'))).toEqual({ type: 'newTask' });
    expect(resolver.resolve(key('/'))).toEqual({ type: 'search' });
    expect(resolver.resolve(key('?'))).toEqual({ type: 'help' });
    expect(resolver.resolve(key('x'))).toBeNull();
  });

  it('jumps to a view with G followed by a letter', () => {
    expect(resolver.resolve(key('g'), 1000)).toBeNull();
    expect(resolver.resolve(key('c'), 1400)).toEqual({ type: 'goto', tab: 'CALENDAR' });
  });

  it('forgets a G that was pressed too long ago', () => {
    resolver.resolve(key('g'), 1000);

    expect(resolver.resolve(key('r'), 1000 + SEQUENCE_TIMEOUT_MS + 1)).toBeNull();
  });

  it('treats the key after G as a fresh press when it is not a view letter', () => {
    resolver.resolve(key('g'), 1000);

    expect(resolver.resolve(key('n'), 1100)).toEqual({ type: 'newTask' });
  });

  it('stays out of the way while typing in a text field', () => {
    const input = document.createElement('input');
    const textarea = document.createElement('textarea');

    expect(resolver.resolve(key('n', { target: input }))).toBeNull();
    expect(resolver.resolve(key('/', { target: textarea }))).toBeNull();
  });

  it('ignores browser shortcuts like Ctrl+N', () => {
    expect(resolver.resolve(key('n', { ctrlKey: true }))).toBeNull();
    expect(resolver.resolve(key('n', { metaKey: true }))).toBeNull();
  });

  it('lets Escape through even inside a text field', () => {
    const input = document.createElement('input');

    expect(resolver.resolve(key('Escape', { target: input }))).toEqual({ type: 'escape' });
  });
});

describe('ShortcutHelpComponent', () => {
  it('lists the shortcuts and closes from the button or the backdrop', () => {
    const fixture = TestBed.createComponent(ShortcutHelpComponent);
    const closed = vi.fn();
    fixture.componentInstance.close.subscribe(closed);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;

    const text = el.textContent ?? '';
    expect(text).toContain('New task in the open board');
    expect(text).toContain('Calendar');
    expect(el.querySelectorAll('kbd').length).toBeGreaterThan(10);

    // Clicking inside the card must not close it
    (el.querySelector('.shortcut-card') as HTMLElement).click();
    expect(closed).not.toHaveBeenCalled();

    (el.querySelector('.shortcut-close') as HTMLButtonElement).click();
    (el.querySelector('.shortcut-overlay') as HTMLElement).click();
    expect(closed).toHaveBeenCalledTimes(2);
  });
});

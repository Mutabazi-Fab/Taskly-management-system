import { Component, output } from '@angular/core';
import { SHORTCUT_GROUPS } from '../../core/shortcuts/keyboard-shortcuts';

/** The "?" overlay that lists every keyboard shortcut. */
@Component({
  selector: 'app-shortcut-help',
  standalone: true,
  template: `
    <div class="shortcut-overlay" (click)="close.emit()">
      <div class="shortcut-card" role="dialog" aria-modal="true" aria-labelledby="shortcut-title"
           (click)="$event.stopPropagation()">
        <div class="shortcut-header">
          <h3 id="shortcut-title">Keyboard shortcuts</h3>
          <button type="button" class="shortcut-close" aria-label="Close" (click)="close.emit()">×</button>
        </div>

        <div class="shortcut-groups">
          @for (group of groups; track group.title) {
            <section>
              <h4>{{ group.title }}</h4>
              <ul>
                @for (shortcut of group.shortcuts; track shortcut.description) {
                  <li>
                    <span class="shortcut-desc">{{ shortcut.description }}</span>
                    <span class="shortcut-keys">
                      @for (key of shortcut.keys; track $index) {
                        @if ($index > 0) { <span class="shortcut-then">then</span> }
                        <kbd>{{ key }}</kbd>
                      }
                    </span>
                  </li>
                }
              </ul>
            </section>
          }
        </div>

        <p class="shortcut-footer">Shortcuts are paused while you type in a text box.</p>
      </div>
    </div>
  `,
  styleUrl: './shortcut-help.css'
})
export class ShortcutHelpComponent {
  readonly close = output<void>();
  protected readonly groups = SHORTCUT_GROUPS;
}

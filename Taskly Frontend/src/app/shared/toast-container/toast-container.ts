import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';

/** Renders the toasts from ToastService. Placed once in the app shell (app.html). */
@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div class="toast-container" role="status" aria-live="polite">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast-card" [class]="toast.type">
          <span class="toast-message">{{ toast.message }}</span>
          <button class="btn-toast-close" type="button" aria-label="Dismiss notification"
                  (click)="toastService.dismiss(toast.id)">×</button>
        </div>
      }
    </div>
  `,
  styleUrl: './toast-container.css'
})
export class ToastContainerComponent {
  protected readonly toastService = inject(ToastService);
}

import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'danger' | 'info';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
}

/**
 * App-wide toast notifications, shown by the single <app-toast-container> in the app shell.
 * Any page can call show() instead of using the browser's blocking alert().
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  static readonly DEFAULT_DURATION_MS = 4000;

  private nextId = 0;
  private readonly _toasts = signal<Toast[]>([]);

  /** The toasts currently on screen, oldest first */
  readonly toasts = this._toasts.asReadonly();

  show(message: string, type: ToastType = 'success', durationMs = ToastService.DEFAULT_DURATION_MS): void {
    const id = ++this.nextId;
    this._toasts.update(list => [...list, { id, message, type }]);
    setTimeout(() => this.dismiss(id), durationMs);
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'danger');
  }

  info(message: string): void {
    this.show(message, 'info');
  }

  dismiss(id: number): void {
    this._toasts.update(list => list.filter(t => t.id !== id));
  }
}

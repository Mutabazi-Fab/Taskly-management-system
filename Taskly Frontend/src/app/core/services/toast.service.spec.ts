import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast.service';
import { ToastContainerComponent } from '../../shared/toast-container/toast-container';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    vi.useFakeTimers();
    service = TestBed.inject(ToastService);
  });

  afterEach(() => vi.useRealTimers());

  it('shows a toast with the given message and type', () => {
    service.error('Something went wrong');

    expect(service.toasts()).toEqual([
      expect.objectContaining({ message: 'Something went wrong', type: 'danger' }),
    ]);
  });

  it('removes a toast automatically after 4 seconds', () => {
    service.success('Saved');

    vi.advanceTimersByTime(ToastService.DEFAULT_DURATION_MS - 1);
    expect(service.toasts().length).toBe(1);

    vi.advanceTimersByTime(1);
    expect(service.toasts().length).toBe(0);
  });

  it('dismisses only the toast that was closed', () => {
    service.success('First');
    service.info('Second');

    service.dismiss(service.toasts()[0].id);

    expect(service.toasts().map(t => t.message)).toEqual(['Second']);
  });
});

describe('ToastContainerComponent', () => {
  it('renders each toast and removes it when its close button is clicked', () => {
    const fixture = TestBed.createComponent(ToastContainerComponent);
    const service = TestBed.inject(ToastService);
    const element = fixture.nativeElement as HTMLElement;

    service.success('Member added to the team.');
    fixture.detectChanges();

    const card = element.querySelector('.toast-card');
    expect(card?.textContent).toContain('Member added to the team.');
    expect(card?.classList).toContain('success');

    (element.querySelector('.btn-toast-close') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(element.querySelector('.toast-card')).toBeNull();
  });
});

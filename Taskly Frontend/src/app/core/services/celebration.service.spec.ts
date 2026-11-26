import { TestBed } from '@angular/core/testing';
import { CelebrationService } from './celebration.service';

describe('CelebrationService', () => {
  let service: CelebrationService;
  let frames: FrameRequestCallback[];

  // Runs the queued animation frames as if `ms` milliseconds had passed since the burst started
  function runFrameAt(ms: number): void {
    const pending = frames;
    frames = [];
    pending.forEach(cb => cb(ms));
  }

  const canvases = () => document.querySelectorAll('canvas.celebration-canvas');

  function mockReducedMotion(reduce: boolean): void {
    vi.stubGlobal('matchMedia', (query: string) => ({ matches: reduce, media: query }));
  }

  beforeEach(() => {
    frames = [];
    vi.stubGlobal('requestAnimationFrame', (cb: FrameRequestCallback) => frames.push(cb));
    // jsdom has no real canvas, so hand back a context that ignores drawing calls
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(new Proxy({}, {
      get: () => () => undefined,
      set: () => true,
    }) as unknown as CanvasRenderingContext2D);
    mockReducedMotion(false);
    service = TestBed.inject(CelebrationService);
  });

  afterEach(() => {
    canvases().forEach(c => c.remove());
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('shows confetti and cleans up when the animation ends', () => {
    service.celebrate({ x: 100, y: 200 });
    expect(canvases().length).toBe(1);

    runFrameAt(0);
    runFrameAt(CelebrationService.DURATION_MS / 2);
    expect(canvases().length).toBe(1);

    runFrameAt(CelebrationService.DURATION_MS);
    expect(canvases().length).toBe(0);
    expect(frames.length).toBe(0);
  });

  it('does not stack bursts when tasks are completed in quick succession', () => {
    service.celebrate();
    service.celebrate();

    expect(canvases().length).toBe(1);
  });

  it('can celebrate again once the previous burst has finished', () => {
    service.celebrate();
    runFrameAt(0);
    runFrameAt(CelebrationService.DURATION_MS);

    service.celebrate();
    expect(canvases().length).toBe(1);
  });

  it('skips the animation for people who prefer reduced motion', () => {
    mockReducedMotion(true);

    service.celebrate();

    expect(canvases().length).toBe(0);
    expect(frames.length).toBe(0);
  });
});

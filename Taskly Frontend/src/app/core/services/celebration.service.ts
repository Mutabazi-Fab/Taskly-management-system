import { Injectable } from '@angular/core';

export interface Point {
  x: number;
  y: number;
}

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  size: number;
  rotation: number;
  spin: number;
  color: string;
}

/**
 * A short confetti burst for finished tasks. Draws on a temporary full-screen canvas that
 * removes itself when the animation ends, so it needs no library and leaves nothing behind.
 * Visitors who ask their system for reduced motion are not shown the animation.
 */
@Injectable({ providedIn: 'root' })
export class CelebrationService {
  static readonly DURATION_MS = 2200;
  static readonly PARTICLE_COUNT = 140;

  private static readonly COLORS = ['#3447AA', '#5B6FD6', '#10B981', '#F59E0B', '#EF4444', '#FBEAEB', '#38BDF8'];
  private static readonly GRAVITY = 0.32;
  private static readonly DRAG = 0.985;

  private active = 0;

  /** Bursts confetti from `origin` (screen coordinates), or from the top middle of the screen. */
  celebrate(origin?: Point): void {
    // Don't stack bursts if someone completes several tasks in a row
    if (this.active > 0 || this.prefersReducedMotion()) return;

    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    canvas.className = 'celebration-canvas';
    canvas.setAttribute('aria-hidden', 'true');
    Object.assign(canvas.style, {
      position: 'fixed', inset: '0', width: '100vw', height: '100vh',
      pointerEvents: 'none', zIndex: '10001'
    });
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    document.body.appendChild(canvas);
    this.active++;

    const start = origin ?? { x: window.innerWidth / 2, y: window.innerHeight * 0.3 };
    const particles = Array.from({ length: CelebrationService.PARTICLE_COUNT }, () => this.createParticle(start));
    let startedAt: number | null = null;

    const frame = (now: number) => {
      startedAt ??= now;
      const elapsed = now - startedAt;

      if (elapsed >= CelebrationService.DURATION_MS) {
        canvas.remove();
        this.active--;
        return;
      }

      // Fade out over the last third of the animation
      const fadeStart = CelebrationService.DURATION_MS * (2 / 3);
      ctx.globalAlpha = elapsed < fadeStart ? 1 : 1 - (elapsed - fadeStart) / (CelebrationService.DURATION_MS - fadeStart);
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      for (const p of particles) {
        p.vx *= CelebrationService.DRAG;
        p.vy = p.vy * CelebrationService.DRAG + CelebrationService.GRAVITY;
        p.x += p.vx;
        p.y += p.vy;
        p.rotation += p.spin;

        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.rotate(p.rotation);
        ctx.fillStyle = p.color;
        ctx.fillRect(-p.size / 2, -p.size / 4, p.size, p.size / 2);
        ctx.restore();
      }

      requestAnimationFrame(frame);
    };

    requestAnimationFrame(frame);
  }

  private createParticle(origin: Point): Particle {
    // Mostly upwards, spread across a wide cone
    const angle = -Math.PI / 2 + (Math.random() - 0.5) * Math.PI * 0.9;
    const speed = 6 + Math.random() * 9;
    const colors = CelebrationService.COLORS;
    return {
      x: origin.x,
      y: origin.y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      size: 6 + Math.random() * 6,
      rotation: Math.random() * Math.PI,
      spin: (Math.random() - 0.5) * 0.3,
      color: colors[Math.floor(Math.random() * colors.length)]
    };
  }

  private prefersReducedMotion(): boolean {
    return typeof window.matchMedia === 'function'
      && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }
}

import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';
import { routes } from './app.routes';

// JWT-shaped string with the given expiry (seconds since epoch). Only "exp" is read client-side.
function fakeToken(exp: number): string {
  return `header.${btoa(JSON.stringify({ sub: 'jane@example.com', exp }))}.signature`;
}

function logInAs(user: { admin: boolean }, exp = Math.floor(Date.now() / 1000) + 3600): void {
  localStorage.setItem('authToken', fakeToken(exp));
  localStorage.setItem('currentUser', JSON.stringify({ id: '1', username: 'jane', email: 'jane@example.com', ...user }));
}

describe('App', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => localStorage.clear());

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('renders pages through the router outlet', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('router-outlet')).not.toBeNull();
  });

  describe('routing and guards', () => {
    it('sends unknown URLs to the login page', async () => {
      const harness = await RouterTestingHarness.create();
      await harness.navigateByUrl('/does-not-exist');
      expect(TestBed.inject(Router).url).toBe('/login');
    });

    it('keeps logged-out visitors out of the dashboard', async () => {
      const harness = await RouterTestingHarness.create();
      await harness.navigateByUrl('/dashboard');
      expect(TestBed.inject(Router).url).toBe('/login');
    });

    it('shows the session-expired notice when an expired token opens the dashboard', async () => {
      logInAs({ admin: false }, Math.floor(Date.now() / 1000) - 60);

      const harness = await RouterTestingHarness.create();
      await harness.navigateByUrl('/dashboard');

      expect(TestBed.inject(Router).url).toBe('/login?reason=expired');
      expect(localStorage.getItem('authToken')).toBeNull();
      expect(harness.routeNativeElement?.textContent).toContain('Your session has expired');
    });

    it('sends non-admins who open /admin back to the dashboard', async () => {
      logInAs({ admin: false });

      const harness = await RouterTestingHarness.create();
      await harness.navigateByUrl('/admin');

      expect(TestBed.inject(Router).url).toBe('/dashboard');
    });
  });
});

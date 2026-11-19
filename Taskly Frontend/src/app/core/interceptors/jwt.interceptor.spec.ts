import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { jwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../services/auth.service';

// Builds a JWT-shaped string whose payload has the given expiry (seconds since epoch).
// The signature is fake: the frontend only reads "exp", the server verifies signatures.
function fakeToken(exp: number): string {
  const payload = btoa(JSON.stringify({ sub: 'jane@example.com', exp }));
  return `header.${payload}.signature`;
}

const inOneHour = () => Math.floor(Date.now() / 1000) + 3600;
const oneHourAgo = () => Math.floor(Date.now() / 1000) - 3600;

describe('jwtInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('adds the Bearer token to outgoing requests', () => {
    localStorage.setItem('authToken', 'abc123');

    http.get('/api/teams').subscribe();

    const req = httpMock.expectOne('/api/teams');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc123');
    req.flush([]);
  });

  it('ends the session and redirects to login when the server answers 401', () => {
    localStorage.setItem('authToken', 'abc123');
    localStorage.setItem('currentUser', '{"id":"1"}');

    http.get('/api/teams').subscribe({ error: () => {} });
    httpMock.expectOne('/api/teams').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(localStorage.getItem('authToken')).toBeNull();
    expect(localStorage.getItem('currentUser')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { reason: 'expired' } });
  });

  it('only redirects once when several requests fail with 401 together', () => {
    localStorage.setItem('authToken', 'abc123');

    http.get('/api/teams').subscribe({ error: () => {} });
    http.get('/api/notifications').subscribe({ error: () => {} });
    httpMock.expectOne('/api/teams').flush(null, { status: 401, statusText: 'Unauthorized' });
    httpMock.expectOne('/api/notifications').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(router.navigate).toHaveBeenCalledTimes(1);
  });

  it('does not log out on a 401 from the login endpoint (that is just a wrong password)', () => {
    localStorage.setItem('authToken', 'old-token');

    http.post('/api/auth/login', {}).subscribe({ error: () => {} });
    httpMock.expectOne('/api/auth/login').flush(
      { message: 'Invalid email or password' }, { status: 401, statusText: 'Unauthorized' });

    expect(localStorage.getItem('authToken')).toBe('old-token');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('does not log out on other errors such as 403 or 500', () => {
    localStorage.setItem('authToken', 'abc123');

    http.delete('/api/users/1').subscribe({ error: () => {} });
    httpMock.expectOne('/api/users/1').flush(null, { status: 403, statusText: 'Forbidden' });

    expect(localStorage.getItem('authToken')).toBe('abc123');
  });
});

describe('AuthService.isTokenValid', () => {
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    authService = TestBed.inject(AuthService);
  });

  it('accepts a token that expires in the future', () => {
    expect(authService.isTokenValid(fakeToken(inOneHour()))).toBe(true);
  });

  it('rejects an expired token', () => {
    expect(authService.isTokenValid(fakeToken(oneHourAgo()))).toBe(false);
  });

  it('rejects missing or malformed tokens', () => {
    expect(authService.isTokenValid(null)).toBe(false);
    expect(authService.isTokenValid('not-a-jwt')).toBe(false);
  });
});

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { LoginComponent } from './login';

describe('LoginComponent demo login', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  function render() {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    return fixture;
  }

  const demoButton = (el: HTMLElement) => el.querySelector('.demo-login-btn') as HTMLButtonElement | null;

  it('shows the demo button when the server offers a demo', () => {
    const fixture = render();
    httpMock.expectOne(r => r.url.endsWith('/auth/demo')).flush({ email: 'demo@taskly.dev', password: 'demo1234' });
    fixture.detectChanges();

    expect(demoButton(fixture.nativeElement)?.textContent).toContain('Try the live demo');
  });

  it('hides the demo button when the demo is turned off', () => {
    const fixture = render();
    httpMock.expectOne(r => r.url.endsWith('/auth/demo')).flush(null, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();

    expect(demoButton(fixture.nativeElement)).toBeNull();
  });

  it('logs in with the demo account and opens the dashboard', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = render();
    httpMock.expectOne(r => r.url.endsWith('/auth/demo')).flush({ email: 'demo@taskly.dev', password: 'demo1234' });
    fixture.detectChanges();

    demoButton(fixture.nativeElement)!.click();

    const login = httpMock.expectOne(r => r.url.endsWith('/auth/login'));
    expect(login.request.body).toEqual({ email: 'demo@taskly.dev', password: 'demo1234' });
    login.flush({ token: 'jwt', email: 'demo@taskly.dev', username: 'Demo User', userId: '1', admin: false });

    expect(navigate).toHaveBeenCalledWith(['/dashboard']);
    // The shared demo email must not overwrite the visitor's own "remember me" email
    expect(localStorage.getItem('taskly_remembered_email')).toBeNull();
  });
});

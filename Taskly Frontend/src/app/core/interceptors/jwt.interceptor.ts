import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);

    // Get the JWT token from browser's local storage
    const token = localStorage.getItem('authToken');

    // If token exists, clone the request and add the Authorization header
    const outgoing = token
        ? req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
        : req;

    return next(outgoing).pipe(
        catchError((error: HttpErrorResponse) => {
            // 401 = the server no longer accepts our token (expired or invalid), so end
            // the session. Login itself is excluded: there a 401 just means a wrong
            // password, and the login form shows that message.
            const isLoginRequest = req.url.includes('/auth/login');
            if (error.status === 401 && token && !isLoginRequest) {
                authService.endExpiredSession();
            }
            return throwError(() => error);
        })
    );
};

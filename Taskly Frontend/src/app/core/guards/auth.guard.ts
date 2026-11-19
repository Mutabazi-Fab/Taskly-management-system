import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
    const router = inject(Router);
    const authService = inject(AuthService);

    // Allow access only with a token that hasn't expired yet
    if (authService.isTokenValid()) {
        return true;
    }

    // A token that exists but has expired means the session timed out: say so on the login page
    const hadToken = !!localStorage.getItem('authToken');
    if (hadToken) {
        authService.clearSession();
        return router.createUrlTree(['/login'], { queryParams: { reason: 'expired' } });
    }

    return router.createUrlTree(['/login']);
};

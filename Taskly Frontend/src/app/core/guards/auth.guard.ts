import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const token = localStorage.getItem('authToken');

    // If a token is present, allow access
    if (token) {
        return true;
    }

    // Otherwise, redirect to login page
    router.navigate(['/login']);
    return false;
};

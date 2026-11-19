import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, User } from '../models/user.model';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private http = inject(HttpClient);
    private router = inject(Router);

    // API URL pointing to our Spring Boot backend
    private apiUrl = window.location.origin.includes('localhost')
        ? 'http://localhost:8080/api'
        : '/api';

    // Reactive signal holding the current logged-in user profile
    currentUser = signal<User | null>(null);

    constructor() {
        // Re-hydrate user state only if the stored token is still valid.
        // An expired token is left for authGuard, which clears it and tells the user why.
        const cachedUser = localStorage.getItem('currentUser');
        if (this.isTokenValid() && cachedUser) {
            this.currentUser.set(JSON.parse(cachedUser));
        }
    }

    // Handles user registration
    register(user: Partial<User> & { password?: string }): Observable<User> {
        return this.http.post<User>(`${this.apiUrl}/users`, user);
    }

    // Handles user login
    login(credentials: { email: string; password?: string }): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, credentials).pipe(
            tap(response => {
                // Store token and cached user data in browser localStorage
                localStorage.setItem('authToken', response.token);

                const userProfile: User = {
                    id: response.userId,
                    username: response.username,
                    email: response.email,
                    admin: response.admin,
                    createdAt: new Date().toISOString()
                };
                localStorage.setItem('currentUser', JSON.stringify(userProfile));

                // Update our reactive signal
                this.currentUser.set(userProfile);
            })
        );
    }

    getAllUsers(): Observable<User[]> {
        return this.http.get<User[]>(`${this.apiUrl}/users`);
    }

    deleteUser(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
    }

    toggleAdmin(id: string): Observable<User> {
        return this.http.patch<User>(`${this.apiUrl}/users/${id}/admin`, {});
    }

    // Log the user out
    logout(): void {
        this.clearSession();
        this.router.navigate(['/login']);
    }

    /**
     * Called when the server rejects our token (HTTP 401): it expired, or the backend
     * restarted with a different JWT secret. Several requests can fail at once, so
     * only the first call does anything.
     */
    endExpiredSession(): void {
        if (!localStorage.getItem('authToken')) {
            return;
        }
        this.clearSession();
        this.router.navigate(['/login'], { queryParams: { reason: 'expired' } });
    }

    // True if a token is stored and its expiry time ("exp" claim) has not passed yet
    isAuthenticated(): boolean {
        return this.isTokenValid();
    }

    isTokenValid(token: string | null = localStorage.getItem('authToken')): boolean {
        if (!token) {
            return false;
        }
        try {
            // A JWT is header.payload.signature; the payload is base64url-encoded JSON.
            // Reading "exp" here is only a convenience check: the server still verifies
            // the signature on every request.
            const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
            const { exp } = JSON.parse(atob(payload));
            return typeof exp === 'number' && exp * 1000 > Date.now();
        } catch {
            return false; // Malformed token
        }
    }

    clearSession(): void {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        this.currentUser.set(null);
    }
}

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
        // Re-hydrate user state if a token and cached user info exist in the browser
        const token = localStorage.getItem('authToken');
        const cachedUser = localStorage.getItem('currentUser');
        if (token && cachedUser) {
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
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        this.currentUser.set(null);
        this.router.navigate(['/login']);
    }

    // Quick helper to check authentication status
    isAuthenticated(): boolean {
        return !!localStorage.getItem('authToken');
    }
}

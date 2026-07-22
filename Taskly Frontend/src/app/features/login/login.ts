import { Component, signal, inject, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  // Mode state
  isSignUp = signal(false);

  // Login Fields
  email = signal('');
  password = signal('');
  showPassword = signal(false);

  // Register Fields
  username = signal('');
  registerEmail = signal('');
  registerPassword = signal('');
  showRegisterPassword = signal(false);

  // Feedback & Loading states
  errorMessage = signal('');
  successMessage = signal('');
  isLoading = signal(false);

  ngOnInit(): void {
    // Monitor URL paths to toggle sliding active class state
    this.route.url.subscribe(url => {
      const path = url[0]?.path;
      this.isSignUp.set(path === 'register');
      // Reset form messages
      this.errorMessage.set('');
      this.successMessage.set('');
    });
  }

  toggleMode(signUp: boolean): void {
    this.router.navigate([signUp ? '/register' : '/login']);
  }

  onLoginSubmit(): void {
    if (!this.email() || !this.password()) {
      this.errorMessage.set('Please fill in all fields.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.authService.login({ email: this.email(), password: this.password() }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Login failed. Please check your credentials.');
      }
    });
  }

  onRegisterSubmit(): void {
    if (!this.username() || !this.registerEmail() || !this.registerPassword()) {
      this.errorMessage.set('Please fill in all fields.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const payload = {
      username: this.username(),
      email: this.registerEmail(),
      password: this.registerPassword()
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set('Registration successful! Redirecting to login...');
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1500);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Registration failed. Email might be already in use.');
      }
    });
  }
}

import { Component, signal, computed, inject, OnInit } from '@angular/core';
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

  // Mode Toggle
  isSignupMode = signal(false);

  // Form Fields
  username = signal('');
  email = signal('');
  password = signal('');
  confirmPassword = signal('');
  showPassword = signal(false);
  showConfirmPassword = signal(false);
  rememberMe = signal(false);

  // State
  errorMessage = signal('');
  successMessage = signal('');
  isLoading = signal(false);

  // Password Strength Evaluation
  passwordStrength = computed(() => {
    const pwd = this.password();
    if (!pwd) return 0;
    let score = 0;
    if (pwd.length >= 6) score += 1;
    if (/[A-Z]/.test(pwd) || /[0-9]/.test(pwd)) score += 1;
    if (/[^A-Za-z0-9]/.test(pwd) && pwd.length >= 8) score += 1;
    return score;
  });

  passwordStrengthText = computed(() => {
    switch (this.passwordStrength()) {
      case 1: return 'Weak';
      case 2: return 'Good';
      case 3: return 'Strong';
      default: return 'Too short';
    }
  });

  passwordsMatch = computed(() => {
    if (!this.confirmPassword()) return true;
    return this.password() === this.confirmPassword();
  });

  ngOnInit(): void {
    // Check route data or query params for signup mode
    const mode = this.route.snapshot.data['mode'] || this.route.snapshot.queryParams['mode'];
    if (mode === 'signup') {
      this.isSignupMode.set(true);
    }

    // Rehydrate remembered email
    const savedEmail = localStorage.getItem('taskly_remembered_email');
    if (savedEmail) {
      this.email.set(savedEmail);
      this.rememberMe.set(true);
    }
  }

  onLoginSubmit(): void {
    if (!this.email() || !this.password()) {
      this.errorMessage.set('Please fill in all fields.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.login({ email: this.email(), password: this.password() }).subscribe({
      next: () => {
        if (this.rememberMe()) {
          localStorage.setItem('taskly_remembered_email', this.email());
        } else {
          localStorage.removeItem('taskly_remembered_email');
        }
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
    if (!this.username() || !this.email() || !this.password() || !this.confirmPassword()) {
      this.errorMessage.set('Please fill in all fields.');
      return;
    }

    if (this.password() !== this.confirmPassword()) {
      this.errorMessage.set('Passwords do not match.');
      return;
    }

    if (this.password().length < 6) {
      this.errorMessage.set('Password must be at least 6 characters.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const payload = {
      username: this.username(),
      email: this.email(),
      password: this.password()
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set('Registration successful! You can now log in.');
        setTimeout(() => {
          this.isSignupMode.set(false);
          this.successMessage.set('');
          this.confirmPassword.set('');
        }, 1500);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Registration failed. Email might be already in use.');
      }
    });
  }
}

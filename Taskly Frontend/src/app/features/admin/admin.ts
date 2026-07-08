import { Component, OnInit, signal, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class AdminComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);

  users = signal<User[]>([]);
  isLoading = signal(false);
  errorMessage = signal('');

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading.set(true);
    this.authService.getAllUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to load system users.');
      }
    });
  }

  toggleAdmin(user: User): void {
    if (user.id === this.authService.currentUser()?.id) {
      alert('You cannot revoke your own admin rights!');
      return;
    }

    this.authService.toggleAdmin(user.id).subscribe({
      next: () => this.loadUsers(),
      error: (err) => alert(err.error?.message || 'Failed to toggle privileges.')
    });
  }

  // Custom Confirmation Modal System
  showConfirmModal = signal(false);
  confirmMessage = signal('');
  confirmAction = signal<(() => void) | null>(null);

  triggerConfirm(message: string, action: () => void): void {
    this.confirmMessage.set(message);
    this.confirmAction.set(() => action);
    this.showConfirmModal.set(true);
  }

  onConfirmSubmit(): void {
    const action = this.confirmAction();
    if (action) {
      action();
    }
    this.showConfirmModal.set(false);
    this.confirmAction.set(null);
  }

  deleteUser(user: User): void {
    if (user.id === this.authService.currentUser()?.id) {
      alert('You cannot delete your own account!');
      return;
    }

    this.triggerConfirm(`Are you sure you want to delete user ${user.username}? This cannot be undone.`, () => {
      this.authService.deleteUser(user.id).subscribe({
        next: () => this.loadUsers(),
        error: (err) => alert(err.error?.message || 'Failed to delete user. Make sure they do not own any active teams.')
      });
    });
  }
}

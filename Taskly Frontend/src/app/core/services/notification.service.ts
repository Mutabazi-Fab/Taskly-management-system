import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  
  // Dynamically resolve base URL based on local vs production environment
  private apiBaseUrl = window.location.origin.includes('localhost')
      ? 'http://localhost:8080/api'
      : '/api';

  getNotifications(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiBaseUrl}/notifications`);
  }

  markAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiBaseUrl}/notifications/${id}/read`, {});
  }

  createJoinRequest(teamId: string, adminId: string): Observable<any> {
    return this.http.post<any>(`${this.apiBaseUrl}/join-requests`, { teamId, adminId });
  }

  approveJoinRequest(requestId: string): Observable<any> {
    return this.http.post<any>(`${this.apiBaseUrl}/join-requests/${requestId}/approve`, {});
  }

  denyJoinRequest(requestId: string, reason: string): Observable<any> {
    return this.http.post<any>(`${this.apiBaseUrl}/join-requests/${requestId}/deny`, { reason });
  }
}

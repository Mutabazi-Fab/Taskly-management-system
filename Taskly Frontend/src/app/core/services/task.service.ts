import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task, Attachment } from '../models/task.model';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private http = inject(HttpClient);
  private apiBaseUrl = window.location.origin.includes('localhost')
      ? 'http://localhost:8080/api'
      : '/api';

  createTask(listId: string, task: {
    title: string;
    description: string;
    priority: string;
    dueDate: string;
    creatorId: string;
    assigneeId: string | null;
  }): Observable<Task> {
    return this.http.post<Task>(`${this.apiBaseUrl}/lists/${listId}/tasks`, task);
  }

  getTasksByList(listId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiBaseUrl}/lists/${listId}/tasks`);
  }

  updateTask(taskId: string, task: Partial<Task>): Observable<Task> {
    return this.http.put<Task>(`${this.apiBaseUrl}/tasks/${taskId}`, task);
  }

  updateTaskStatus(taskId: string, status: string): Observable<Task> {
    return this.http.patch<Task>(`${this.apiBaseUrl}/tasks/${taskId}/status`, { status });
  }

  deleteTask(taskId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/tasks/${taskId}`);
  }

  getTasks(assigneeId?: string, status?: string): Observable<Task[]> {
    let params = new HttpParams();
    if (assigneeId) {
      params = params.set('assignee', assigneeId);
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<Task[]>(`${this.apiBaseUrl}/tasks`, { params });
  }

  addAttachment(taskId: string, attachment: { fileName: string; fileUrl: string }): Observable<Attachment> {
    return this.http.post<Attachment>(`${this.apiBaseUrl}/tasks/${taskId}/attachments`, attachment);
  }

  getAttachments(taskId: string): Observable<Attachment[]> {
    return this.http.get<Attachment[]>(`${this.apiBaseUrl}/tasks/${taskId}/attachments`);
  }

  deleteAttachment(attachmentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/attachments/${attachmentId}`);
  }
}

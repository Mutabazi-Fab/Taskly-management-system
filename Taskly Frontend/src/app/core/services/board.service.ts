import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Board, TaskList, BoardStats } from '../models/board.model';

@Injectable({
  providedIn: 'root'
})
export class BoardService {
  private http = inject(HttpClient);
  private apiBaseUrl = window.location.origin.includes('localhost')
      ? 'http://localhost:8080/api'
      : '/api';

  createBoard(teamId: string, board: { name: string }): Observable<Board> {
    return this.http.post<Board>(`${this.apiBaseUrl}/teams/${teamId}/boards`, board);
  }

  getBoardsByTeam(teamId: string): Observable<Board[]> {
    return this.http.get<Board[]>(`${this.apiBaseUrl}/teams/${teamId}/boards`);
  }

  deleteBoard(boardId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/boards/${boardId}`);
  }

  getBoardStats(boardId: string): Observable<BoardStats> {
    return this.http.get<BoardStats>(`${this.apiBaseUrl}/boards/${boardId}/stats`);
  }

  createList(boardId: string, list: { name: string; position: number }): Observable<TaskList> {
    return this.http.post<TaskList>(`${this.apiBaseUrl}/boards/${boardId}/lists`, list);
  }

  getListsByBoard(boardId: string): Observable<TaskList[]> {
    return this.http.get<TaskList[]>(`${this.apiBaseUrl}/boards/${boardId}/lists`);
  }

  deleteList(listId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/lists/${listId}`);
  }
}

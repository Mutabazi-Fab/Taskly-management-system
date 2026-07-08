import { Component, OnInit, signal, computed, inject, HostListener } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { TeamService } from '../../core/services/team.service';
import { BoardService } from '../../core/services/board.service';
import { TaskService } from '../../core/services/task.service';
import { NotificationService } from '../../core/services/notification.service';
import { Team, TeamMember } from '../../core/models/team.model';
import { Board, TaskList, BoardStats } from '../../core/models/board.model';
import { Task, TaskPriority, TaskStatus } from '../../core/models/task.model';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private teamService = inject(TeamService);
  private boardService = inject(BoardService);
  private taskService = inject(TaskService);
  private notificationService = inject(NotificationService);
  private router = inject(Router);

  // Profile
  currentUser = this.authService.currentUser;

  // Data Signals
  teams = signal<Team[]>([]);
  selectedTeam = signal<Team | null>(null);
  teamMembers = signal<TeamMember[]>([]);

  boards = signal<Board[]>([]);
  selectedBoard = signal<Board | null>(null);
  boardStats = signal<BoardStats | null>(null);

  lists = signal<TaskList[]>([]);
  // Maps listId to its list of tasks
  tasksMap = signal<Record<string, Task[]>>({});

  // Tab View selector
  activeTab = signal<'DASHBOARD' | 'MY_TASKS' | 'BOARDS' | 'CALENDAR' | 'TEAM' | 'REPORTS' | 'SETTINGS'>('DASHBOARD');

  // Notifications state
  notifications = signal<any[]>([]);
  unreadNotifications = computed(() => this.notifications().filter(n => !n.isRead));
  unreadNotificationsCount = computed(() => this.unreadNotifications().length);
  showNotificationsDropdown = signal(false);

  // Request to Join modal state
  showJoinRequestModal = signal(false);
  selectedTeamToJoin = signal<string>('');
  selectedAdminToJoin = signal<string>('');
  adminsList = computed(() => this.allUsers().filter(u => u.admin));

  // Rejection / Denial state
  showDenialReasonModal = signal(false);
  denialReasonInput = signal<string>('');
  selectedRequestToDeny = signal<string | null>(null);

  // Toast System
  toasts = signal<{ id: string; message: string; type: 'success' | 'danger' | 'info' }[]>([]);

  showToast(message: string, type: 'success' | 'danger' | 'info' = 'success'): void {
    const id = Math.random().toString(36).substring(2, 9);
    this.toasts.update(list => [...list, { id, message, type }]);
    setTimeout(() => {
      this.toasts.update(list => list.filter(t => t.id !== id));
    }, 4000);
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

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    // Close notifications dropdown if clicked outside the container
    if (!target.closest('.notifications-container') && !target.closest('.btn-approve-req') && !target.closest('.btn-deny-req')) {
      this.showNotificationsDropdown.set(false);
    }
    // Close profile dropdown if clicked outside its container
    if (!target.closest('.profile-avatar-container')) {
      this.showProfileDropdown.set(false);
    }
  }

  // Sidebar Minimized State
  isSidebarMinimized = signal(false);

  // Profile Dropdown Toggle
  showProfileDropdown = signal(false);

  // System-wide users
  allUsers = signal<User[]>([]);

  // System-wide tasks
  allTasks = signal<Task[]>([]);

  // Sorted list of all tasks with due dates
  getTasksWithDueDate = computed(() => {
    return this.allTasks()
      .filter(t => t.dueDate)
      .sort((a, b) => new Date(a.dueDate!).getTime() - new Date(b.dueDate!).getTime());
  });

  // Calculate dynamic team activities from actual database tasks
  teamActivities = computed(() => {
    const users = this.allUsers();
    return this.allTasks()
      .slice()
      .sort((a, b) => new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime())
      .slice(0, 5)
      .map(task => {
        const assigneeUser = users.find(u => u.id === task.assigneeId);
        const creatorUser = users.find(u => u.id === task.creatorId);
        const assigneeName = assigneeUser ? assigneeUser.username : 'Someone';
        const creatorName = creatorUser ? creatorUser.username : 'Someone';

        let actor = creatorName;
        let action = `added a new task "${task.title}"`;
        if (task.status === 'DONE') {
          actor = assigneeUser ? assigneeUser.username : creatorName;
          action = `completed "${task.title}"`;
        } else if (task.status === 'IN_PROGRESS') {
          actor = assigneeUser ? assigneeUser.username : creatorName;
          action = `started working on "${task.title}"`;
        }

        // Calculate a basic human-readable time ago relative to local system clock
        let timeAgo = 'Just now';
        if (task.createdAt) {
          const diff = new Date().getTime() - new Date(task.createdAt).getTime();
          const mins = Math.floor(diff / 60000);
          const hours = Math.floor(mins / 60);
          const days = Math.floor(hours / 24);
          if (days > 0) {
            timeAgo = `${days}d ago`;
          } else if (hours > 0) {
            timeAgo = `${hours}h ago`;
          } else if (mins > 0) {
            timeAgo = `${mins}m ago`;
          }
        }

        return {
          id: task.id,
          actor,
          action,
          timeAgo
        };
      });
  });

  // Dynamic upcoming tasks with due dates in the future
  upcomingEvents = computed(() => {
    const now = new Date();
    now.setHours(0,0,0,0);
    return this.allTasks()
      .filter(t => t.dueDate && new Date(t.dueDate) >= now && t.status !== 'DONE')
      .sort((a, b) => new Date(a.dueDate!).getTime() - new Date(b.dueDate!).getTime())
      .slice(0, 3);
  });

  // Calculate workloads and progress per team member
  teamWorkloads = computed(() => {
    const members = this.teamMembers();
    const tasks = this.allTasks();
    return members.map(member => {
      const activeTasks = tasks.filter(t => t.assigneeId === member.userId && t.status !== 'DONE');
      const completedTasks = tasks.filter(t => t.assigneeId === member.userId && t.status === 'DONE');
      const total = activeTasks.length + completedTasks.length;
      const progress = total > 0 ? Math.round((completedTasks.length / total) * 100) : 0;
      return {
        username: member.username,
        activeCount: activeTasks.length,
        progress
      };
    });
  });

  // User's personal tasks and metrics
  myTasks = signal<Task[]>([]);
  myTasksCount = computed(() => this.myTasks().length);
  inProgressTasksCount = computed(() => this.myTasks().filter(t => t.status === 'IN_PROGRESS').length);
  completedTasksCount = computed(() => this.myTasks().filter(t => t.status === 'DONE').length);
  overdueTasksCount = computed(() => {
    const now = new Date();
    now.setHours(0,0,0,0);
    return this.myTasks().filter(t => t.status !== 'DONE' && t.dueDate && new Date(t.dueDate) < now).length;
  });

  isCurrentTeamOwner = computed(() => {
    const currentUserId = this.currentUser()?.id;
    const members = this.teamMembers();
    const userInTeam = members.find(m => m.userId === currentUserId);
    return userInTeam?.role === 'OWNER';
  });

  // Filter Signals
  selectedPriorityFilter = signal<string>('ALL');
  selectedAssigneeFilter = signal<string>('ALL');

  // Modal Dialog UI Signals
  showCreateTeam = signal(false);
  newTeamName = signal('');

  showAddMember = signal(false);
  newMemberEmail = signal('');
  newMemberRole = signal('MEMBER');

  showCreateBoard = signal(false);
  newBoardName = signal('');

  showCreateList = signal(false);
  newListName = signal('');

  showCreateTask = signal(false);
  selectedListForNewTask = signal<string>('');
  newTaskTitle = signal('');
  newTaskDesc = signal('');
  newTaskPriority = signal('MEDIUM');
  newTaskDueDate = signal('');
  newTaskAssigneeId = signal('');

  ngOnInit(): void {
    this.loadTeams();
    this.loadMyTasks();
    this.loadAllUsers();
    this.loadAllTasks();
    this.loadNotifications();
  }

  logout(): void {
    this.authService.logout();
  }

  loadAllUsers(): void {
    this.authService.getAllUsers().subscribe({
      next: (data) => this.allUsers.set(data)
    });
  }

  loadAllTasks(): void {
    this.taskService.getTasks().subscribe({
      next: (tasks) => this.allTasks.set(tasks)
    });
  }

  loadMyTasks(): void {
    const userId = this.currentUser()?.id;
    if (!userId) return;
    this.taskService.getTasks(userId).subscribe({
      next: (tasks) => this.myTasks.set(tasks)
    });
  }

  toggleTaskCompletion(task: Task, event: any): void {
    const isChecked = event.target.checked;
    const nextStatus: TaskStatus = isChecked ? 'DONE' : 'TODO';
    
    this.taskService.updateTaskStatus(task.id, nextStatus).subscribe({
      next: () => {
        this.loadMyTasks();
        this.loadAllTasks();
        const board = this.selectedBoard();
        if (board) this.loadBoardData(board.id);
      }
    });
  }

  // --- Core Data Loaders ---

  loadTeams(): void {
    this.teamService.getTeams().subscribe({
      next: (data) => {
        this.teams.set(data);
        if (data.length > 0 && !this.selectedTeam()) {
          this.selectTeam(data[0]);
        }
      }
    });
  }

  selectTeam(team: Team): void {
    this.selectedTeam.set(team);
    this.selectedBoard.set(null);
    this.boards.set([]);
    this.lists.set([]);
    this.tasksMap.set({});
    this.boardStats.set(null);

    this.teamService.getTeamMembers(team.id).subscribe({
      next: (members) => this.teamMembers.set(members)
    });

    this.boardService.getBoardsByTeam(team.id).subscribe({
      next: (boards) => {
        this.boards.set(boards);
        if (boards.length > 0) {
          this.selectBoard(boards[0]);
        }
      }
    });
  }

  selectBoard(board: Board): void {
    this.selectedBoard.set(board);
    this.loadBoardData(board.id);
  }

  loadBoardData(boardId: string): void {
    // Sync personal tasks
    this.loadMyTasks();
    this.loadAllTasks();

    // Load board stats
    this.boardService.getBoardStats(boardId).subscribe({
      next: (stats) => this.boardStats.set(stats)
    });

    // Load lists & columns
    this.boardService.getListsByBoard(boardId).subscribe({
      next: (listsData) => {
        // Sort lists by position
        const sorted = listsData.sort((a, b) => a.position - b.position);
        this.lists.set(sorted);

        // Fetch tasks for each list
        const initialMap: Record<string, Task[]> = {};
        sorted.forEach(list => {
          this.taskService.getTasksByList(list.id).subscribe({
            next: (tasks) => {
              initialMap[list.id] = tasks;
              this.tasksMap.set({ ...this.tasksMap(), [list.id]: tasks });
            }
          });
        });
      }
    });
  }

  // --- Filters ---
  getFilteredTasks(listId: string): Task[] {
    const listTasks = this.tasksMap()[listId] || [];
    return listTasks.filter(task => {
      const matchPriority = this.selectedPriorityFilter() === 'ALL' || task.priority === this.selectedPriorityFilter();
      const matchAssignee = this.selectedAssigneeFilter() === 'ALL' || task.assigneeId === this.selectedAssigneeFilter();
      return matchPriority && matchAssignee;
    });
  }

  // --- CRUD Modals Handlers ---

  onCreateTeamSubmit(): void {
    if (!this.newTeamName().trim()) return;
    const userId = this.currentUser()?.id;
    if (!userId) return;

    this.teamService.createTeam({ name: this.newTeamName(), ownerId: userId }).subscribe({
      next: (team) => {
        this.showCreateTeam.set(false);
        this.newTeamName.set('');
        this.loadTeams();
      }
    });
  }

  onAddMemberSubmit(): void {
    const team = this.selectedTeam();
    if (!team || !this.newMemberEmail().trim()) return;

    this.teamService.addMember(team.id, {
      email: this.newMemberEmail(),
      role: this.newMemberRole()
    }).subscribe({
      next: () => {
        this.showAddMember.set(false);
        this.newMemberEmail.set('');
        this.selectTeam(team);
      },
      error: (err) => alert(err.error?.message || 'Failed to add member.')
    });
  }

  onCreateBoardSubmit(): void {
    const team = this.selectedTeam();
    if (!team || !this.newBoardName().trim()) return;

    this.boardService.createBoard(team.id, { name: this.newBoardName() }).subscribe({
      next: (board) => {
        this.showCreateBoard.set(false);
        this.newBoardName.set('');
        this.selectTeam(team);
      }
    });
  }

  onCreateListSubmit(): void {
    const board = this.selectedBoard();
    if (!board || !this.newListName().trim()) return;

    const nextPosition = this.lists().length;
    this.boardService.createList(board.id, {
      name: this.newListName(),
      position: nextPosition
    }).subscribe({
      next: () => {
        this.showCreateList.set(false);
        this.newListName.set('');
        this.loadBoardData(board.id);
      }
    });
  }

  openCreateTaskModal(listId: string): void {
    this.selectedListForNewTask.set(listId);
    this.showCreateTask.set(true);
  }

  onCreateTaskSubmit(): void {
    const listId = this.selectedListForNewTask();
    const board = this.selectedBoard();
    const creatorId = this.currentUser()?.id;
    if (!listId || !board || !creatorId || !this.newTaskTitle().trim()) return;

    this.taskService.createTask(listId, {
      title: this.newTaskTitle(),
      description: this.newTaskDesc(),
      priority: this.newTaskPriority(),
      dueDate: this.newTaskDueDate() || new Date().toISOString().split('T')[0],
      creatorId: creatorId,
      assigneeId: this.newTaskAssigneeId() ? this.newTaskAssigneeId() : null
    }).subscribe({
      next: () => {
        this.showCreateTask.set(false);
        this.newTaskTitle.set('');
        this.newTaskDesc.set('');
        this.newTaskPriority.set('MEDIUM');
        this.newTaskDueDate.set('');
        this.newTaskAssigneeId.set('');
        this.loadBoardData(board.id);
        this.loadAllTasks();
      }
    });
  }

  // --- Task Operations ---

  moveTask(task: Task, direction: 'left' | 'right'): void {
    const lists = this.lists();
    const currentListIndex = lists.findIndex(l => l.id === task.listId);
    if (currentListIndex === -1) return;

    let targetListIndex = currentListIndex + (direction === 'left' ? -1 : 1);
    if (targetListIndex < 0 || targetListIndex >= lists.length) return;

    const targetList = lists[targetListIndex];
    // Find target status mapping based on target list name (fallback to TODO/IN_PROGRESS/DONE)
    let nextStatus: TaskStatus = 'TODO';
    const nameLower = targetList.name.toLowerCase();
    if (nameLower.includes('done') || nameLower.includes('complete')) {
      nextStatus = 'DONE';
    } else if (nameLower.includes('progress') || nameLower.includes('doing')) {
      nextStatus = 'IN_PROGRESS';
    }

    // Call update task (move task list ID & PATCH status)
    this.taskService.updateTask(task.id, { listId: targetList.id }).subscribe({
      next: () => {
        this.taskService.updateTaskStatus(task.id, nextStatus).subscribe({
          next: () => {
            const board = this.selectedBoard();
            if (board) this.loadBoardData(board.id);
            this.loadAllTasks();
          }
        });
      }
    });
  }

  deleteTask(task: Task): void {
    this.triggerConfirm('Are you sure you want to delete this task?', () => {
      this.taskService.deleteTask(task.id).subscribe({
        next: () => {
          this.showToast("Task deleted successfully.", "success");
          const board = this.selectedBoard();
          if (board) this.loadBoardData(board.id);
          this.loadAllTasks();
        }
      });
    });
  }

  onDeleteTeam(team: Team, event: Event): void {
    event.stopPropagation(); // Prevent card selection click event
    this.triggerConfirm("Do you actually want to delete this team?", () => {
      this.teamService.deleteTeam(team.id).subscribe({
        next: () => {
          this.showToast("Team deleted successfully.", "success");
          if (this.selectedTeam()?.id === team.id) {
            this.selectedTeam.set(null);
            this.selectedBoard.set(null);
            this.lists.set([]);
            this.teamMembers.set([]);
          }
          this.loadTeams();
          this.loadAllTasks();
          this.loadMyTasks();
          this.loadNotifications();
        },
        error: (err) => this.showToast(err.error?.message || "Failed to delete team.", "danger")
      });
    });
  }

  deleteList(listId: string): void {
    this.triggerConfirm('Deleting this list will delete all tasks inside it. Proceed?', () => {
      this.boardService.deleteList(listId).subscribe({
        next: () => {
          this.showToast("Column deleted successfully.", "success");
          const board = this.selectedBoard();
          if (board) this.loadBoardData(board.id);
        }
      });
    });
  }

  // SVG stats stroke math calculations
  getStrokeDashArray(count: number): string {
    const stats = this.boardStats();
    if (!stats) return '0 100';
    const total = stats.todoCount + stats.inProgressCount + stats.doneCount;
    if (total === 0) return '0 100';
    const percentage = (count / total) * 100;
    return `${percentage} ${100 - percentage}`;
  }

  getStrokeDashOffset(count1: number, count2?: number): number {
    const stats = this.boardStats();
    if (!stats) return 0;
    const total = stats.todoCount + stats.inProgressCount + stats.doneCount;
    if (total === 0) return 0;
    
    let sumBefore = count1;
    if (count2 !== undefined) {
      sumBefore += count2;
    }
    return -((sumBefore / total) * 100);
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (data) => this.notifications.set(data),
      error: (err) => console.error("Failed to load notifications", err)
    });
  }

  markNotificationRead(id: string): void {
    this.notificationService.markAsRead(id).subscribe({
      next: () => {
        this.loadNotifications();
      }
    });
  }

  submitJoinRequest(): void {
    if (!this.selectedTeamToJoin() || !this.selectedAdminToJoin()) {
      this.showToast("Please select a team and an administrator.", "danger");
      return;
    }
    this.notificationService.createJoinRequest(this.selectedTeamToJoin(), this.selectedAdminToJoin()).subscribe({
      next: () => {
        this.showToast("Your request to join the team has been submitted successfully!", "success");
        this.showJoinRequestModal.set(false);
        this.selectedTeamToJoin.set('');
        this.selectedAdminToJoin.set('');
        this.loadNotifications();
      },
      error: (err) => {
        this.showToast(err.error?.message || "Failed to submit request.", "danger");
      }
    });
  }

  approveRequest(requestId: string): void {
    this.notificationService.approveJoinRequest(requestId).subscribe({
      next: () => {
        this.showToast("Request approved! Member has been enrolled in the team.", "success");
        this.loadNotifications();
        this.loadTeams();
        this.loadAllTasks();
        this.loadMyTasks();
      },
      error: (err) => this.showToast(err.error?.message || "Failed to approve request.", "danger")
    });
  }

  openDenialModal(requestId: string): void {
    this.selectedRequestToDeny.set(requestId);
    this.denialReasonInput.set('');
    this.showDenialReasonModal.set(true);
  }

  submitDenyRequest(): void {
    const requestId = this.selectedRequestToDeny();
    const reason = this.denialReasonInput();
    if (!requestId) return;
    if (!reason.trim()) {
      this.showToast("Please specify a reason for denial.", "danger");
      return;
    }
    this.notificationService.denyJoinRequest(requestId, reason).subscribe({
      next: () => {
        this.showToast("Request denied.", "success");
        this.showDenialReasonModal.set(false);
        this.selectedRequestToDeny.set(null);
        this.denialReasonInput.set('');
        this.loadNotifications();
      },
      error: (err) => this.showToast(err.error?.message || "Failed to deny request.", "danger")
    });
  }
}

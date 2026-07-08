export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface Task {
    id: string;
    title: string;
    description: string;
    status: TaskStatus;
    priority: TaskPriority;
    dueDate: string;
    listId: string;
    assigneeId: string | null;
    creatorId: string;
    createdAt: string;
}

export interface Attachment {
    id: string;
    fileName: string;
    fileUrl: string;
    taskId: string;
    createdAt: string;
}

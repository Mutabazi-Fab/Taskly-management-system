export interface Board {
    id: string;
    name: string;
    teamId: string;
    createdAt: string;
}

export interface TaskList {
    id: string;
    name: string;
    position: number;
    boardId: string;
}

export interface BoardStats {
    todoCount: number;
    inProgressCount: number;
    doneCount: number;
}
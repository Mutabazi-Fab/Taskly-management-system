export interface User {
    id: string;
    username: string;
    email: string;
    admin: boolean;
    createdAt: string;
}

export interface AuthResponse {
    token: string;
    email: string;
    username: string;
    userId: string;
    admin: boolean;
}

export type TeamRole = 'OWNER' | 'MEMBER';

export interface Team {
    id: string;
    name: string;
    ownerId: string;
    ownerUsername: string;
    createdAt: string;
}

export interface TeamMember {
    id: string;
    userId: string;
    username: string;
    email: string;
    role: TeamRole;
}
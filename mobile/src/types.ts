export type Role = 'ADMINISTRATOR' | 'MANAGER' | 'CREW_LEADER' | 'WORKER' | 'OFFICE_WORKER';
export type User = { id: string; companyId: string; email: string; displayName: string; role: Role; mustChangePassword: boolean };
export type Session = { accessToken: string; refreshToken: string; expiresIn: number; user: User };
export type Resource = { id: string; type: string; data: Record<string, unknown>; version: number; createdAt: string; updatedAt: string; deletedAt?: string };
export type Page<T> = { items: T[]; page: number; size: number; totalElements: number; totalPages: number };
export type Operation = { operationId: string; entityType: string; entityId: string; action: 'CREATE'|'UPDATE'|'DELETE'; version?: number; localTimestamp: string; payload: Record<string, unknown> };


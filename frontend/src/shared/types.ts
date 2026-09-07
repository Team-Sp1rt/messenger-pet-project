export interface AuthContextType {
    token: string | null
    userId: number | null
    login: (token: string) => void
    logout: () => void
}

export class ApiError extends Error {
    status: number;
    constructor(status: number, message: string) {
        super(message);
        this.status = status;
    }
}
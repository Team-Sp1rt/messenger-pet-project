export type AuthContextType = {
    token: string | null;
    login: (token: string) => void;
    logout: () => void;
};

export class ApiError extends Error {
    status: number;
    constructor(status: number, message: string) {
        super(message);
        this.status = status;
    }
}
const BASE_URL = 'http://localhost:8080/api';

export class ApiError extends Error {
    status: number;
    constructor(status: number, message: string) {
        super(message);
        this.status = status;
    }
}

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
    const res = await fetch(`${BASE_URL}${path}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
    });

    const data = await res.json().catch(() => null);

    if (!res.ok) {
        const message = data?.message ?? `Request failed with status ${res.status}`;
        throw new ApiError(res.status, message);
    }

    return data as T;
}
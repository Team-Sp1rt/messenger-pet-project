import { apiFetch } from "../../../shared/api/Client";
import type { AuthResponse, LoginPayload, RegisterPayload } from "../types";

export function loginRequest(payload: LoginPayload) {
    return apiFetch<AuthResponse>('/auth/login', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
}
 
export function registerRequest(payload: RegisterPayload) {
    return apiFetch<AuthResponse>('/auth/register', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
}
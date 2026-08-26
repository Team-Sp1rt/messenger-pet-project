import { useEffect } from "react";
import { refreshRequest } from "../../features/auth/api/Auth";
import { getTokenExpiryMs } from "../utils/jwt";

const BUFFER_MS = 15 * 1000;

export function useTokenRefresh(
    token: string | null,
    login: (token: string) => void,
    logout: () => void
) {
    useEffect(() => {
        if (!token) return;

        const expiryMs = getTokenExpiryMs(token);
        if (expiryMs === null) return;

        let delay = expiryMs - Date.now() - BUFFER_MS;
        if (delay <= 0) delay = 0;

        const timeout = setTimeout(async () => {
            try {
                const { token: newToken } = await refreshRequest();
                login(newToken);
            } catch {
                logout();
            }
        }, delay);

        return () => clearTimeout(timeout);
    }, [token, login, logout]);
}
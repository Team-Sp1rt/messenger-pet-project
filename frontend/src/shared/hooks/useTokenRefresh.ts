import { useEffect } from "react";
import { refreshRequest } from "../../features/auth/api/Auth";

export function useTokenRefresh(
    token: string | null,
    login: (token: string) => void,
    logout: () => void
) {
    useEffect(() => {
        if (!token) return;

        refreshRequest()
            .then(({ token: newToken }) => login(newToken))
            .catch(() => {
                logout();
            });
    }, []);
}
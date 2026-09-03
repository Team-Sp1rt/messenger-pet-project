import { createContext, useContext, useEffect, useMemo, useState } from "react";

import { registerUnauthorizedHandler } from "../api/Client";
import type { AuthContextType } from "../types";
import { isTokenExpired, getUserIdFromToken } from "../utils/jwt";
import { useTokenRefresh } from "../hooks/useTokenRefresh";

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({
    children,
}: {
    children: React.ReactNode;
}) {
    const [token, setToken] = useState<string | null>(() => {
            const stored = localStorage.getItem("token");
            if (!stored) return null;

            if (isTokenExpired(stored)) {
                localStorage.removeItem("token");
                return null;
            }
            
            return stored;
        }
    );

    const login = (token: string) => {
        localStorage.setItem("token", token);
        setToken(token);
    };

    const logout = () => {
        localStorage.removeItem("token");
        setToken(null);
    };

    useEffect(() => {
        registerUnauthorizedHandler(logout);
    }, []);

    useTokenRefresh(token, login, logout);

    const userId = useMemo(() => (token ? getUserIdFromToken(token) : null), [token]);
    
    return (
        <AuthContext.Provider
            value={{ token, userId, login, logout }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used inside AuthProvider");
    }

    return context;
}
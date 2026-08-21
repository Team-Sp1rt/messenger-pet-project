import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"

import AuthPage from "./features/auth/pages/AuthPage"
import ChatsPage from "./features/chats/pages/ChatsPage"

import { useAuth } from "./shared/context/AuthContext";

function AppRoutes() {
    const { token } = useAuth();
    
    return (
        <BrowserRouter>
            <Routes>
                {!token
                    ? <Route path="/auth" element={<AuthPage />}/>
                    : <Route path="/auth" element={<Navigate to="/chats" />}/>
                }

                <Route path="/" element={<Navigate to={token ? "/chats" : "/auth"} />} />
                
                {token
                    ? <Route path="/chats" element={<ChatsPage />} />
                    : <Route path="/chats" element={<Navigate to="/auth" />}/>
                }
            </Routes>
        </BrowserRouter>
    )
}

export default AppRoutes

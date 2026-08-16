import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import AuthPage from "./features/auth/pages/AuthPage";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/auth" element={<AuthPage />}/>
                <Route path="/" element={<Navigate to="/auth" />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App;
export interface OverlayPanelProps {
    isSignUpMode: boolean;
    onSwitchToSignUp: () => void;
    onSwitchToSignIn: () => void;
}

export interface LoginPayload {
    login: string;
    password: string;
}
 
export interface RegisterPayload {
    username: string;
    login: string;
    password: string;
    birthday: string;
}
 
export interface AuthResponse {
    token: string;
    user: {
        id: string;
        username: string;
    };
}
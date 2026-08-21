import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { KeyRound, LockIcon } from 'lucide-react';

import styles from '../styles/Form.module.css';

import { loginRequest } from '../api/Auth';
import { ApiError } from '../../../shared/types';
import { useAuth } from '../../../shared/context/AuthContext';

function LoginForm() {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();

        const formData = new FormData(e.currentTarget);

        const loginValue = formData.get('login_field') as string;
        const password = formData.get('password_field') as string;

        setError(null);
        setIsSubmitting(true);

        try {
            const { token } = await loginRequest({
                login: loginValue,
                password,
            });

            login(token);

            navigate("/chats");
        } catch (err) {
            setError(
                err instanceof ApiError
                    ? err.message
                    : 'Something went wrong. Please try again.'
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
            <h1 className={styles.title}>Sign in to Vibe</h1>

            <div className={styles.inputWrapper}>
                <KeyRound size={18} />
                <input
                    className={styles.input}
                    name="login_field"
                    type="text"
                    placeholder="Login"
                    required
                />
            </div>

            <div className={styles.inputWrapper}>
                <LockIcon size={18} />
                <input
                    className={styles.input}
                    name="password_field"
                    type="password"
                    placeholder="Password"
                    required
                    minLength={8}
                />
            </div>
 
            <a href="#" className={styles.forgotLink}>Forgot your password?</a>
 
            {error && <p className={styles.errorText}>{error}</p>}

            <button className={styles.submitButton} type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Signing in...' : 'Sign in'}
            </button>
        </form>
    )
}

export default LoginForm;
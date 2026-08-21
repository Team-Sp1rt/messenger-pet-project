import { useState } from 'react';
import { KeyRound, LockIcon } from 'lucide-react';

import styles from '../styles/Form.module.css';

import { loginRequest } from '../api/Auth';
import { ApiError } from '../../../shared/api/Client';

function LoginForm() {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();

        const formData = new FormData(e.currentTarget);

        const login = formData.get('login_field') as string;
        const password = formData.get('password_field') as string;

        setError(null);
        setIsSubmitting(true);

        try {
            const { token } = await loginRequest({ login, password });
            localStorage.setItem('token', token);
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
                {isSubmitting ? 'Creating account...' : 'Sign up'}
            </button>
        </form>
    )
}

export default LoginForm;
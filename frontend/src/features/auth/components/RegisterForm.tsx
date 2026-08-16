import { useState } from 'react';
import { KeyRound, LockIcon, UserRound } from 'lucide-react';

import styles from '../styles/Form.module.css';

import { registerRequest } from '../api/Auth';
import { ApiError } from '../../../shared/api/Client';

function RegisterForm() {
    const [username, setUsername] = useState('');
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        try {
            const { token } = await registerRequest({ username, login, password });
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
            <h1 className={styles.title}>Create Account</h1>

            <div className={styles.inputWrapper}>
                <UserRound size={18} />
                <input
                    className={styles.input}
                    name="username_field"
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
            </div>

            <div className={styles.inputWrapper}>
                <KeyRound size={18} />
                <input
                    className={styles.input}
                    name="login_field"
                    type="text"
                    placeholder="Login"
                    value={login}
                    onChange={(e) => setLogin(e.target.value)}
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
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    minLength={8}
                />
            </div>

            {error && <p className={styles.errorText}>{error}</p>}

            <button className={styles.submitButton} type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Creating account...' : 'Sign up'}
            </button>
        </form>
    )
}

export default RegisterForm;
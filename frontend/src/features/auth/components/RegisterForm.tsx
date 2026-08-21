import { useRef, useState } from 'react';
import { Calendar, KeyRound, LockIcon, UserRound } from 'lucide-react';

import styles from '../styles/Form.module.css';

import { registerRequest } from '../api/Auth';
import { ApiError } from '../../../shared/types';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../shared/context/AuthContext';

function RegisterForm() {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const dateRef = useRef<HTMLInputElement>(null);
    
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();

        const formData = new FormData(e.currentTarget);

        const username = formData.get('username_field') as string;
        const loginValue = formData.get('login_field') as string;
        const password = formData.get('password_field') as string;
        const birthday = formData.get('birthday_field') as string;

        setError(null);
        setIsSubmitting(true);

        try {
            const { token } = await registerRequest({ username, login: loginValue, password, birthday });
            
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
            <h1 className={styles.title}>Create Account</h1>

            <div className={styles.inputWrapper}>
                <UserRound size={18} />
                <input
                    className={styles.input}
                    name="username_field"
                    type="text"
                    placeholder="Username"
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

            <div className={styles.inputWrapper}>
                <Calendar size={18} onClick={() => dateRef.current?.showPicker()}/>
                <input
                    ref={dateRef}
                    className={styles.input}
                    name="birthday_field"
                    type="date"
                    required
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
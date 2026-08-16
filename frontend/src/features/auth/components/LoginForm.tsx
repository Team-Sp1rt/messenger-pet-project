import { KeyRound, LockIcon } from 'lucide-react';
import styles from '../styles/Form.module.css';

function LoginForm() {
    return (
        <form className={styles.form}>
            <h1 className={styles.title}>Sign in to Vibe</h1>

            <div className={styles.inputWrapper}>
                <KeyRound size={18} />
                <input className={styles.input} name="login" type="text" placeholder="Login" />
            </div>
 
            <div className={styles.inputWrapper}>
                <LockIcon size={18} />
                <input className={styles.input} name="password" type="password" placeholder="Password" />
            </div>
 
            <a href="#" className={styles.forgotLink}>Forgot your password?</a>
 
            <button className={styles.submitButton}>Sign in</button>
        </form>
    )
}

export default LoginForm;
import { useState } from 'react';
import styles from '../styles/AuthPage.module.css';

import LoginForm from "../components/LoginForm";
import RegisterForm from '../components/RegisterForm';
import OverlayPanel from '../components/OverlayPanel';

function AuthPage() {
    const [isSignUpMode, setIsSignUpMode] = useState(false);
    const [showSignUpForm, setShowSignUpForm] = useState(false);

    const switchToSignUp = () => {
        setIsSignUpMode(true);

        setTimeout(() => {
            setShowSignUpForm(true);
        }, 300);
    };

    const switchToSignIn = () => {
        setIsSignUpMode(false);

        setTimeout(() => {
            setShowSignUpForm(false);
        }, 300);
    };

    return (
        <div className={styles.outerContainer}>
            <div
                className={`${styles.card} ${
                    isSignUpMode ? styles.signUpMode : ''
                }`}
            >
                <div>
                    <div className={`${styles.formWrapper} ${styles.signInWrapper}`}>
                        {!showSignUpForm && <LoginForm />}
                    </div>

                    <div className={`${styles.formWrapper} ${styles.signUpWrapper}`}>
                        {showSignUpForm && <RegisterForm />}
                    </div>
                </div>

                <div className={styles.overlayContainer}>
                    <OverlayPanel
                        isSignUpMode={isSignUpMode}
                        onSwitchToSignUp={switchToSignUp}
                        onSwitchToSignIn={switchToSignIn}
                    />
                </div>
            </div>
        </div>
    );
}

export default AuthPage;
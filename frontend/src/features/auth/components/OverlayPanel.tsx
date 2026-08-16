import type { OverlayPanelProps } from '../types';
import styles from '../styles/OverlayPanel.module.css';

function OverlayPanel({ isSignUpMode, onSwitchToSignUp, onSwitchToSignIn }: OverlayPanelProps) {
    return (
        <div className={`${styles.overlay} ${isSignUpMode ? styles.overlaySignUp : ''}`}>
            <div className={styles.panel}>
                <h2>Welcome Back!</h2>
                <p>To keep connected with us please login with your personal info</p>
                <button className={styles.ghostButton} onClick={onSwitchToSignIn}>
                    Sign in
                </button>
            </div>

            <div className={styles.panel}>
                <h2>Hello, Friend!</h2>
                <p>Enter your personal details and start your journey with us</p>
                <button className={styles.ghostButton} onClick={onSwitchToSignUp}>
                    Sign up
                </button>
            </div>
        </div>
    );
}

export default OverlayPanel;
import { useEffect, type CSSProperties } from 'react'

interface NoticeBannerProps {
    message: string
    kind?: 'error' | 'info'
    autoHideMs?: number
    onDismiss: () => void
}

export default function NoticeBanner({ message, kind = 'error', autoHideMs = 5000, onDismiss }: NoticeBannerProps) {
    const styles : CSSProperties = {
        position: 'fixed',
        top: 16,
        left: '50%',
        transform: 'translateX(-50%)',
        zIndex: 9999,
        display: 'flex',
        alignItems: 'center',
        padding: '10px 16px',
        borderRadius: 8,
        fontSize: 14,
        color: '#fff',
        background: kind === 'error' ? '#d13438' : '#2b2b2b',
        boxShadow: '0 4px 12px rgba(0,0,0,0.25)',
        cursor: 'pointer'
    };

    useEffect(() => {
        if (!autoHideMs) return
        const t = setTimeout(onDismiss, autoHideMs)
        return () => clearTimeout(t)
    }, [message, autoHideMs, onDismiss])

    return (
        <div
            role="alert"
            aria-live="assertive"
            onClick={onDismiss}
            style={styles}
        >
            {message}
        </div>
    )
}
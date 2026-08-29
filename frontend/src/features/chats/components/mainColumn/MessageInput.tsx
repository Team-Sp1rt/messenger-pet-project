import { useState, type KeyboardEvent } from 'react'
import { Send } from 'lucide-react'
import styles from '../../styles/ChatWindow.module.scss'

interface MessageInputProps {
    onSend: (content: string) => void
}

function MessageInput({ onSend }: MessageInputProps) {
    const [value, setValue] = useState('')

    const handleSend = () => {
        const trimmed = value.trim()
        if (!trimmed) return
        onSend(trimmed)
        setValue('')
    }

    const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault()
            handleSend()
        }
    }

    return (
        <div className={styles.inputArea}>
            <textarea
                className={styles.textInput}
                placeholder='Написать сообщение...'
                rows={1}
                value={value}
                onChange={(e) => setValue(e.target.value)}
                onKeyDown={handleKeyDown}
            />
            <button className={styles.sendButton} onClick={handleSend} disabled={!value.trim()}>
                <Send size={18} />
            </button>
        </div>
    )
}

export default MessageInput
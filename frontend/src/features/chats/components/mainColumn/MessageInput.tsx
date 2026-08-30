import { useRef, useState, type KeyboardEvent } from 'react'
import { Send } from 'lucide-react'
import styles from '../../styles/ChatWindow.module.scss'

interface MessageInputProps {
    onSend: (content: string) => void
}

function MessageInput({ onSend }: MessageInputProps) {
    const [isEmpty, setIsEmpty] = useState(true);
    const editableRef = useRef<HTMLDivElement>(null);

    const handleSend = () => {
        const el = editableRef.current;
        if (!el) return;

        const trimmed = el.innerText.trim();
        if (!trimmed) return;

        onSend(trimmed);
        el.textContent = '';
        setIsEmpty(true);
    }

    const handleInput = () => {
        const el = editableRef.current;
        setIsEmpty(!el || el.innerText.trim().length === 0);
    }

    const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    }

    return (
        <div className={styles.inputArea}>
            <div className={styles.textInputWrapper}>
                {isEmpty && <p className={styles.placeholder}>Message</p>}
                <div
                    ref={editableRef}
                    className={styles.textInput}
                    contentEditable
                    role='textbox'
                    aria-multiline='true'
                    onInput={handleInput}
                    onKeyDown={handleKeyDown}
                    suppressContentEditableWarning
                />
            </div>
            <button className={styles.sendButton} onClick={handleSend} disabled={isEmpty}>
                <Send size={18} />
            </button>
        </div>
    )
}

export default MessageInput;
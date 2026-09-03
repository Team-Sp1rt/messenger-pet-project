import { useEffect, useRef, useState, type KeyboardEvent } from 'react'
import { Send, X, Pencil, Check } from 'lucide-react'
import styles from '../../styles/ChatWindow.module.scss'
import type { ChatMessageUI } from '../../types'

interface MessageInputProps {
    onSend: (content: string) => void
    editingMessage: ChatMessageUI | null
    onSubmitEdit: (content: string) => void
    onCancelEdit: () => void
}

function MessageInput({ onSend, editingMessage, onSubmitEdit, onCancelEdit }: MessageInputProps) {
    const [isEmpty, setIsEmpty] = useState(true);
    const editableRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const el = editableRef.current;
        if (!el) return;

        if (editingMessage) {
            el.textContent = editingMessage.content;
            setIsEmpty(editingMessage.content.trim().length === 0);
            el.focus();

            const range = document.createRange();
            range.selectNodeContents(el);
            range.collapse(false);
            const selection = window.getSelection();
            selection?.removeAllRanges();
            selection?.addRange(range);
        } else {
            el.textContent = '';
            setIsEmpty(true);
        }
    }, [editingMessage]);

    const handleSend = () => {
        const el = editableRef.current;
        if (!el) return;

        const trimmed = el.innerText.trim();
        if (!trimmed) return;

        if (editingMessage) {
            onSubmitEdit(trimmed);
        } else {
            onSend(trimmed);
        }

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

        if (e.key === 'Escape' && editingMessage) {
            e.preventDefault();
            onCancelEdit();
        }
    }

    return (
        <div className={styles.inputArea}>
            <div className={styles.textInputWrapper}>
                {editingMessage && (
                    <div className={styles.editWrapper}>
                        <Pencil size={24} className={styles.editingBannerIcon} />
                        <div className={styles.editingBanner}>
                            <div className={styles.editingBannerText}>
                                <span className={styles.editingBannerTitle}>Edit Message</span>
                                <span className={styles.editingBannerPreview}>{editingMessage.content}</span>
                            </div>
                        </div>
                        <button className={styles.cancelEditButton} onClick={onCancelEdit}>
                            <X size={24} />
                        </button>
                    </div>
                )}

                <div className={styles.inputRow}>
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
            </div>

            <button className={styles.sendButton} onClick={handleSend} disabled={isEmpty}>
                {editingMessage ? <Check size={18} /> : <Send size={18} />}
            </button>
        </div>
    )
}

export default MessageInput;
import styles from '../../styles/ChatWindow.module.scss'
import ChatWindowHeader from './ChatWindowHeader'
import MessagesList from './MessagesList'
import MessageInput from './MessageInput'
import type { Chat, ChatMessageUI } from '../../types'
import { useEffect } from 'react'

interface ChatWindowProps {
    chat: Chat | null
    messages: ChatMessageUI[]
    hasMoreHistory: boolean
    isLoadingOlder: boolean
    onLoadMore: () => void
    onSend: (content: string) => void
    onClose: () => void
}

function ChatWindow({ chat, messages, hasMoreHistory, isLoadingOlder, onLoadMore, onSend, onClose }: ChatWindowProps) {
    useEffect(() => {
        if (!chat) return;

        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === 'Escape') {
                onClose();
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [chat, onClose]);
    
    if (!chat) {
        return (
            <div className={styles.chatWindow}>
                <div className={styles.emptyState}>
                    <span className={styles.emptyStateTitle}>Выберите чат</span>
                    <span className={styles.emptyStateSubtitle}>
                        Начните переписку из списка слева или найдите собеседника через поиск
                    </span>
                </div>
            </div>
        )
    }

    return (
        <div className={styles.chatWindow}>
            <ChatWindowHeader chat={chat} />
            <MessagesList
                chatId={chat.id}
                messages={messages}
                hasMore={hasMoreHistory}
                isLoadingOlder={isLoadingOlder}
                onLoadMore={onLoadMore}
            />
            <MessageInput onSend={onSend} />
        </div>
    )
}

export default ChatWindow;
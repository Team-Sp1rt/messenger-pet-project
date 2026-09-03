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
    onEditMessage: (message: ChatMessageUI) => void
    onDeleteMessage: (messageId: string) => void
    editingMessage: ChatMessageUI | null
    onSubmitEdit: (content: string) => void
    onCancelEdit: () => void
}

function ChatWindow({ chat, messages, hasMoreHistory, isLoadingOlder, onLoadMore, onSend, onClose, onEditMessage, onDeleteMessage, editingMessage, onSubmitEdit, onCancelEdit }: ChatWindowProps) {
    useEffect(() => {
        if (!chat) return;

        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === 'Escape' && !editingMessage) {
                onClose();
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [chat, onClose, editingMessage]);
    
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
                onEditMessage={onEditMessage}
                onDeleteMessage={onDeleteMessage}
            />
            <MessageInput
                onSend={onSend}
                editingMessage={editingMessage}
                onSubmitEdit={onSubmitEdit}
                onCancelEdit={onCancelEdit}
            />
        </div>
    )
}

export default ChatWindow;
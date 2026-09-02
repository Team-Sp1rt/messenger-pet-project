import styles from '../../styles/ChatWindow.module.scss'
import ChatWindowHeader from './ChatWindowHeader'
import MessagesList from './MessagesList'
import MessageInput from './MessageInput'
import type { Chat, ChatMessageUI } from '../../types'

interface ChatWindowProps {
    chat: Chat | null
    messages: ChatMessageUI[]
    hasMoreHistory: boolean
    isLoadingOlder: boolean
    onLoadMore: () => void
    onSend: (content: string) => void
}

function ChatWindow({ chat, messages, hasMoreHistory, isLoadingOlder, onLoadMore, onSend }: ChatWindowProps) {
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
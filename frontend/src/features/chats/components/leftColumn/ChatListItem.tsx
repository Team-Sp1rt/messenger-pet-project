import type { Chat } from '../../types'
import { getAvatarGradient, getInitial } from '../../utils/avatar'
import styles from '../../styles/ChatsLeftColumn.module.scss'

interface ChatListItemProps {
    chat: Chat
    isActive: boolean
    onClick: () => void
}

function ChatListItem({ chat, isActive, onClick }: ChatListItemProps) {
    return (
        <div
            className={`${styles.chatItem} ${isActive ? styles.chatItemActive : ''}`}
            onClick={onClick}
        >
            <div className={styles.chatAvatar} style={{ background: getAvatarGradient(chat.id) }}>
                {getInitial(chat.name)}
            </div>

            <div className={styles.chatInfo}>
                <div className={styles.chatTopRow}>
                    <h3 className={styles.chatName}>{chat.name}</h3>
                    <span className={styles.chatTime}>{chat.time}</span>
                </div>

                <div className={styles.chatBottomRow}>
                    <span className={styles.chatLastMessage}>{chat.lastMessage}</span>
                </div>
            </div>
        </div>
    )
}

export default ChatListItem
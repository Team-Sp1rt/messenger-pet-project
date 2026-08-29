import styles from '../styles/ChatsLeftColumn.module.scss'
import type { ChatListItemProps } from '../types'

const avatarGradients = [
    'linear-gradient(135deg, #ff9a56, #ff6b6b)',
    'linear-gradient(135deg, #6a82fb, #fc5c7d)',
    'linear-gradient(135deg, #43cea2, #185a9d)',
    'linear-gradient(135deg, #f7971e, #ffd200)',
    'linear-gradient(135deg, #8e2de2, #4a00e0)',
]

function getAvatarGradient(id: string) {
    return avatarGradients[id.charCodeAt(0) % avatarGradients.length]
}

function getInitial(name: string) {
    return name.trim().charAt(0).toUpperCase()
}

function ChatListItem({ chat }: ChatListItemProps) {
    return (
        <div className={styles.chatItem}>
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
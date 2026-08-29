import styles from '../../styles/ChatWindow.module.scss'
import { getAvatarGradient, getInitial } from '../../utils/avatar'
import type { Chat } from '../../types'

interface ChatWindowHeaderProps {
    chat: Chat
}

function ChatWindowHeader({ chat }: ChatWindowHeaderProps) {
    return (
        <div className={styles.header}>
            <div className={styles.headerAvatar} style={{ background: getAvatarGradient(chat.id) }}>
                {getInitial(chat.name)}
            </div>
            <div className={styles.headerInfo}>
                <h3 className={styles.headerName}>{chat.name}</h3>
                <span className={styles.headerStatus}>был(а) недавно</span>
            </div>
        </div>
    )
}

export default ChatWindowHeader
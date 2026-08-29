import type { ChatMessageUI } from '../../types'
import styles from '../../styles/ChatWindow.module.scss'

interface MessagesListProps {
    messages: ChatMessageUI[]
}

function MessagesList({ messages }: MessagesListProps) {
    return (
        <div className={styles.messagesAreaWrapper}>
            <div className={styles.messagesArea}>
                {messages.map((message) => (
                    <div
                        key={message.id}
                        className={`${styles.messageRow} ${message.isOwn ? styles.own : styles.other}`}
                    >
                        <div className={`${styles.bubble} ${message.isOwn ? styles.bubbleOwn : styles.bubbleOther}`}>
                            {message.content}
                            <span className={styles.bubbleTime}>{message.time}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}

export default MessagesList
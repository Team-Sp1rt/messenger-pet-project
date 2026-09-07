import styles from '../../styles/ChatsLeftColumn.module.scss'
import ChatListItem from './ChatListItem'
import type { Chat } from '../../types'

interface ListChatsProps {
    chats: Chat[]
    activeChatId: string | null
    onSelectChat: (chatId: string) => void
}

function ListChats({ chats, activeChatId, onSelectChat }: ListChatsProps) {
    return (
        <div className={styles.transition}>
            <div className={styles.chats}>
                {chats.map((chat) => (
                    <ChatListItem
                        key={chat.id}
                        chat={chat}
                        isActive={chat.id === activeChatId}
                        onClick={() => onSelectChat(chat.id)}
                    />
                ))}
            </div>
        </div>
    )
}

export default ListChats;
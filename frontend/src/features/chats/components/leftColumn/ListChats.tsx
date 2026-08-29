import { useState } from 'react'
import styles from '../../styles/ChatsLeftColumn.module.scss'
import ChatFolders from './ChatFolders'
import ChatListItem from './ChatListItem'
import type { Chat, Folder } from '../../types'

const folders: Folder[] = [
    { id: 'all', label: 'All' },
    { id: 'hz', label: 'Hz' },
]

interface ListChatsProps {
    chats: Chat[]
    activeChatId: string | null
    onSelectChat: (chatId: string) => void
}

function ListChats({ chats, activeChatId, onSelectChat }: ListChatsProps) {
    const [activeFolder, setActiveFolder] = useState('all')

    return (
        <div className={styles.transition}>
            <ChatFolders folders={folders} activeFolderId={activeFolder} onSelect={setActiveFolder} />

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

export default ListChats
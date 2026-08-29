import { useState } from 'react'
import styles from '../styles/ChatsLeftColumn.module.scss'
import ChatFolders from './ChatFolders'
import ChatListItem from './ChatListItem'
import type { Chat, Folder } from '../types'

const folders: Folder[] = [
    { id: 'all', label: 'All' },
    { id: 'hz', label: 'Hz'}
]

const chats: Chat[] = [
    { id: '1', name: 'Илья Варламов', lastMessage: 'Скинул фото с митинга', time: '14:32' },
    { id: '2', name: 'Рабочий чат', lastMessage: 'Совещание перенесли на завтра', time: '13:10' },
    { id: '3', name: 'Мама', lastMessage: 'Позвони, как освободишься', time: 'Вчера' }
]

function ListChats() {
    const [activeFolder, setActiveFolder] = useState('all')

    return (
        <div className={styles.transition}>
            <ChatFolders folders={folders} activeFolderId={activeFolder} onSelect={setActiveFolder} />

            <div className={styles.chats}>
                {chats.map((chat) => (
                    <ChatListItem key={chat.id} chat={chat} />
                ))}
            </div>
        </div>
    )
}

export default ListChats;
export interface Folder {
    id: string
    label: string
    count?: number
}

export interface Chat {
    id: string
    name: string
    lastMessage: string
    time: string
}

export interface ChatListItemProps {
    chat: Chat
}

export interface ChatFoldersProps {
    folders: Folder[]
    activeFolderId: string
    onSelect: (id: string) => void
}
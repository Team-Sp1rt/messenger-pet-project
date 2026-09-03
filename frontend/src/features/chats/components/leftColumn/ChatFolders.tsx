import styles from '../../styles/ChatsLeftColumn.module.scss'
import type { Folder } from '../../types'

interface ChatFoldersProps {
    folders: Folder[],
    activeFolderId: string,
    onSelect: (id: string) => void
}

function ChatFolders({ folders, activeFolderId, onSelect }: ChatFoldersProps) {
    return (
        <div className={styles.chatFoldersWrapper}>
            <div className={styles.chatFolders}>
                {folders.map((folder) => (
                    <button
                        key={folder.id}
                        className={`${styles.folderItem} ${activeFolderId === folder.id ? styles.folderActive : ''}`}
                        onClick={() => onSelect(folder.id)}
                    >
                        {folder.label}
                        {folder.count !== undefined && (
                            <span className={styles.folderBadge}>{folder.count}</span>
                        )}
                    </button>
                ))}
            </div>
        </div>
    )
}

export default ChatFolders;
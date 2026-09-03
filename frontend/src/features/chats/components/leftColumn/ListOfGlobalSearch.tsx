import type { UserSummary } from '../../types'
import { getAvatarGradient, getInitial } from '../../utils/avatar'
import styles from '../../styles/ChatsLeftColumn.module.scss'

interface ListOfGlobalSearchProps {
    query: string
    isSearching: boolean
    results: UserSummary[]
    onSelectUser: (user: UserSummary) => void
}

function ListOfGlobalSearch({ query, isSearching, results, onSelectUser }: ListOfGlobalSearchProps) {
    const hasQuery = query.trim().length > 0;

    return (
        <div className={styles.globalSearchWrapper}>
            <div className={styles.globalSearchHeader}>
                <span className={styles.globalSearchTitle}>Global Search</span>
                <button className={styles.globalSearchShowMore}>Show More</button>
            </div>

            <div className={styles.globalSearch}>
                {hasQuery && isSearching && (
                    <div className={styles.globalSearchEmpty}>Поиск...</div>
                )}

                {hasQuery && !isSearching && results.length === 0 && (
                    <div className={styles.globalSearchEmpty}>Никого не найдено</div>
                )}

                {hasQuery && !isSearching && results.map((user) => (
                    <div
                        key={user.id}
                        className={styles.globalSearchItem}
                        onMouseDown={(e) => {
                            e.preventDefault()
                            onSelectUser(user)
                        }}
                    >
                        <div className={styles.globalSearchAvatar} style={{ background: getAvatarGradient(String(user.id)) }}>
                            {getInitial(user.username)}
                        </div>

                        <div className={styles.globalSearchInfo}>
                            <span className={styles.globalSearchName}>{user.username}</span>
                            <span className={styles.globalSearchHandle}>@{user.username.toLowerCase()}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}

export default ListOfGlobalSearch;
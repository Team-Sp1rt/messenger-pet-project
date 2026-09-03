import type { UserSummary } from '../../types'
import styles from '../../styles/ChatsLeftColumn.module.scss'

interface SearchDropdownProps {
    isSearching: boolean
    results: UserSummary[]
    onSelectUser: (user: UserSummary) => void
}

function SearchDropdown({ isSearching, results, onSelectUser }: SearchDropdownProps) {
    return (
        <div className={styles.searchDropdown}>
            {isSearching && (
                <div className={styles.searchDropdownEmpty}>Поиск...</div>
            )}

            {!isSearching && results.length === 0 && (
                <div className={styles.searchDropdownEmpty}>Никого не найдено</div>
            )}

            {!isSearching && results.map((user) => (
                <div
                    key={user.id}
                    className={styles.searchDropdownItem}
                    onMouseDown={(e) => {
                        e.preventDefault()
                        onSelectUser(user)
                    }}
                >
                    {user.username}
                </div>
            ))}
        </div>
    )
}

export default SearchDropdown;
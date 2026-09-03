import type { UserSummary } from '../../types'
import { getAvatarGradient, getInitial } from '../../utils/avatar'
import styles from '../../styles/ChatsLeftColumn.module.scss'
import { useState } from 'react';

interface ListOfGlobalSearchProps {
    query: string,
    isSearching: boolean,
    results: UserSummary[],
    onSelectUser: (user: UserSummary) => void
}

function ListOfGlobalSearch({ query, isSearching, results, onSelectUser }: ListOfGlobalSearchProps) {
    const [isShowMore, setIsShowMore] = useState(false);
    
    const hasQuery = query.trim().length > 0;
    const visibleResults = isShowMore ? results : results.slice(0, 5);
    const showMoreButtonVisible = results.length > 5;

    return (
        <div className={styles.globalSearchWrapper}>
            <div className={styles.globalSearchHeader}>
                <span className={styles.globalSearchTitle}>Global Search</span>
                {showMoreButtonVisible && (
                    <button className={styles.globalSearchShowMore} onClick={() => setIsShowMore((prev) => !prev)}>
                        {isShowMore ? 'Show Less' : 'Show More'}
                    </button>
                )}
            </div>

            <div className={styles.globalSearch}>
                {hasQuery && isSearching && (
                    <div className={styles.globalSearchEmpty}>Поиск...</div>
                )}

                {hasQuery && !isSearching && results.length === 0 && (
                    <div className={styles.globalSearchEmpty}>Никого не найдено</div>
                )}

                {hasQuery && !isSearching && visibleResults.map((user) => (
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
// ChatsHeader.tsx
import { Menu, Search } from 'lucide-react'
import styles from '../styles/ChatsLeftColumn.module.scss'
import { useEffect, useRef, useState } from 'react'
import { searchUsersRequest } from '../api/chats'
import type { UserSummary } from '../types'

interface ChatsHeaderProps {
    onSelectUser: (user: UserSummary) => void
}

function ChatsHeader({ onSelectUser }: ChatsHeaderProps) {
    const [isFocused, setIsFocused] = useState(false)
    const [query, setQuery] = useState('')
    const [results, setResults] = useState<UserSummary[]>([])
    const [isSearching, setIsSearching] = useState(false)
    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)

    useEffect(() => {
        if (debounceRef.current) clearTimeout(debounceRef.current)

        const trimmed = query.trim()
        if (!trimmed) {
            setResults([])
            setIsSearching(false)
            return
        }

        setIsSearching(true)
        debounceRef.current = setTimeout(async () => {
            try {
                const res = await searchUsersRequest({ username: trimmed })
                setResults(res.items)
            } catch {
                setResults([])
            } finally {
                setIsSearching(false)
            }
        }, 1000)

        return () => {
            if (debounceRef.current) clearTimeout(debounceRef.current)
        }
    }, [query])

    const handleSelect = (user: UserSummary) => {
        onSelectUser(user)
        setQuery('')
        setResults([])
    }

    const showDropdown = isFocused && query.trim().length > 0

    return (
        <div className={styles.leftMainHeader}>
            <div className={styles.burgerButton}>
                <Menu />
            </div>
            <div className={styles.searchInput}>
                <div className={styles.leftIconSearch}>
                    <Search className={`${isFocused ? styles.activeBorder : ''}`} />
                </div>
                <input
                    name='search_people'
                    type='text'
                    placeholder='Search'
                    className={styles.input}
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    onFocus={() => setIsFocused(true)}
                    onBlur={() => setIsFocused(false)}
                />

                {showDropdown && (
                    <div className={styles.searchDropdown}>
                        {isSearching && (
                            <div className={styles.searchDropdownEmpty}>Поиск...</div>
                        )}
                        {!isSearching && results.length === 0 && (
                            <div className={styles.searchDropdownEmpty}>Никого не найдено</div>
                        )}
                        {!isSearching && results.map((user) => (
                            <button
                                key={user.id}
                                className={styles.searchDropdownItem}
                                onMouseDown={(e) => {
                                    e.preventDefault()
                                    handleSelect(user)
                                }}
                            >
                                {user.username}
                            </button>
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}

export default ChatsHeader
import { ArrowLeft, LogOut, Menu, Search } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useAuth } from '../../../../shared/context/AuthContext'
import styles from '../../styles/ChatsLeftColumn.module.scss'

interface ChatsHeaderProps {
    query: string,
    onQueryChange: (value: string) => void,
    isFocused: boolean,
    setIsFocused: (isFocused: boolean) => void,
    reset: () => void
}

function ChatsHeader({ query, onQueryChange, isFocused, setIsFocused, reset }: ChatsHeaderProps) {
    const { logout } = useAuth();
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        if (!isMenuOpen) return;

        const handleClickOutside = (e: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
                setIsMenuOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [isMenuOpen]);

    return (
        <div className={styles.leftMainHeader}>
            {query === "" ?
                <div className={styles.menuButtonWrapper} ref={menuRef}>
                    <div className={styles.menuButton} onClick={() => setIsMenuOpen((prev) => !prev)}>
                        <Menu />
                    </div>

                    {isMenuOpen && (
                        <div className={styles.burgerMenu}>
                            <div
                                className={styles.burgerMenuItem}
                                onClick={() => {
                                    setIsMenuOpen(false);
                                    logout();
                                }}
                            >
                                <LogOut size={20} />
                                <span>Log out</span>
                            </div>
                        </div>
                    )}
                </div>
                :
                <div className={styles.menuButton}
                    onClick={() => {
                        reset();
                        setIsFocused(false);
                    }}
                >
                    <ArrowLeft size={28}/>
                </div>
            }

            <div className={styles.searchInput}>
                <div className={styles.leftIconSearch}>
                    <Search className={`${isFocused ? styles.activeBorder : ''}`} />
                </div>
                <input
                    name='search_people'
                    type='text'
                    placeholder='Search'
                    autoComplete="off"
                    className={styles.input}
                    value={query}
                    onChange={(e) => onQueryChange(e.target.value)}
                    onClick={() => setIsFocused(true)}
                    onBlur={() => setIsFocused(false)}
                />
            </div>
        </div>
    )
}

export default ChatsHeader;
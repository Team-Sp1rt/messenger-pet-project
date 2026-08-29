import { Menu, Search } from 'lucide-react'
import styles from '../styles/ChatsLeftColumn.module.scss'
import { useState } from 'react';

function ChatsHeader() {
    const [isFocused, setIsFocused] = useState(false);
    
    return (
        <div className={styles.leftMainHeader}>
            <div className={styles.burgerButton}>
                <Menu />
            </div>
            <div className={styles.searchInput}>
                <div className={styles.leftIconSearch}>
                    <Search className={`${isFocused ? styles.activeBorder : ''}`}/>
                </div>
                <input name='search_people'
                    type='text'
                    placeholder='Search'
                    className={styles.input}
                    onFocus={() => setIsFocused(true)}
                    onBlur={() => setIsFocused(false)}
                />
            </div>
        </div>
    )
}

export default ChatsHeader;
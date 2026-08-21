import { Menu, Search } from 'lucide-react'
import styles from '../styles/ChatsHeader.module.scss'

function ChatsHeader() {
    return (
        <div className={styles.LeftMainHeaderWrapper}>
            <div className={styles.LeftMainHeader}>
                <div className={styles.burgerButton}>
                    <Menu />
                </div>
                <div className={styles.searchInput}>
                    <div className={styles.leftIconSearch}>
                        <Search />
                    </div>
                    <input name='search_people' type='text' placeholder='Search' className={styles.input} />
                </div>
            </div>
        </div>
    )
}

export default ChatsHeader

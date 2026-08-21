import ChatsHeader from '../components/ChatsHeader';
import styles from '../styles/Chats.module.css'

function ChatsPage() {
    return (
        <div className={styles.bodyWrapper}>
            <div className={styles.body} />
            <div className={styles.leftColumnMainWrapper}>
                <div className={styles.leftColumnMain}>
                    <ChatsHeader />
                </div>
            </div>
        </div>
    )
}

export default ChatsPage;
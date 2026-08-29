import ChatsHeader from '../components/ChatsHeader';
import ListChats from '../components/ListChats';
import styles from '../styles/Chats.module.css'

function ChatsPage() {
    return (
        <div className={styles.bodyWrapper}>
            <div className={styles.body} />
            <div className={styles.leftColumnMainWrapper}>
                <div className={styles.leftColumnMain}>
                    <ChatsHeader />

                    <ListChats />
                </div>
            </div>
        </div>
    )
}

export default ChatsPage;
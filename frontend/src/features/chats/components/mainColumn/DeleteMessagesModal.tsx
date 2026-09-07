import styles from '../../styles/ChatWindow.module.scss';

interface DeleteMessageModalProps {
    onCancel: () => void,
    onConfirm: () => void
}

function DeleteMessageModal({onCancel, onConfirm}: DeleteMessageModalProps) {
    return (
        <div className={styles.overlay} onClick={onCancel}>
            <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
                <h2>Delete message</h2>
                <p>Are you sure you want to delete this message?</p>

                <div className={styles.actions}>
                    <button className={styles.cancel} onClick={onCancel}>
                        CANCEL
                    </button>

                    <button className={styles.delete} onClick={onConfirm}>
                        DELETE
                    </button>
                </div>
            </div>
        </div>
    );
}

export default DeleteMessageModal;
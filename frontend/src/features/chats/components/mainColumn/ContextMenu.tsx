import { useEffect, useRef } from 'react'
import styles from '../../styles/ContextMenu.module.scss'

export interface ContextMenuItem {
    key: string,
    label: string,
    danger?: boolean,
    icon: React.ReactNode,
    onClick: () => void
}

interface ContextMenuProps {
    x: number,
    y: number,
    items: ContextMenuItem[],
    onClose: () => void
}

function ContextMenu({ x, y, items, onClose }: ContextMenuProps) {
    const menuRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
                onClose();
            }
        };

        const handleEscape = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };

        document.addEventListener('mousedown', handleClickOutside);
        document.addEventListener('keydown', handleEscape);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside)
            document.removeEventListener('keydown', handleEscape)
        };
    }, [onClose]);

    const adjustedStyle = (() => {
        const menuWidth = 220;
        const menuHeight = items.length * 40 + 16;
        const left = x + menuWidth > window.innerWidth ? window.innerWidth - menuWidth - 8 : x;
        const top = y + menuHeight > window.innerHeight ? window.innerHeight - menuHeight - 8 : y;
        return { left, top };
    })();

    return (
        <div className={styles.contextMenu} style={adjustedStyle} ref={menuRef}>
            {items.map((item) => (
                <button
                    key={item.key}
                    className={`${styles.item} ${item.danger ? styles.danger : ''}`}
                    onClick={() => {
                        item.onClick();
                        onClose();
                    }}
                >
                    <span className={styles.icon}>{item.icon}</span>
                    <span>{item.label}</span>
                </button>
            ))}
        </div>
    )
}

export default ContextMenu;
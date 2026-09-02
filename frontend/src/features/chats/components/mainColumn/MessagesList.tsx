import type { ChatMessageUI } from '../../types'
import styles from '../../styles/ChatWindow.module.scss'
import { useEffect, useLayoutEffect, useRef } from 'react'

interface MessagesListProps {
    chatId: string,
    messages: ChatMessageUI[],
    hasMore: boolean,
    isLoadingOlder: boolean,
    onLoadMore: () => void | Promise<void>
}

const BOTTOM_THRESHOLD_PX = 120;

function MessagesList({ chatId, messages, hasMore, isLoadingOlder, onLoadMore }: MessagesListProps) {
    const scrollContainerRef = useRef<HTMLDivElement>(null);
    const sentinelRef = useRef<HTMLDivElement>(null);
    const prevChatIdRef = useRef<string | null>(null);
    const isLoadingRef = useRef(false);
    const hasInitiallyScrolledRef = useRef(false);

    const prevMessagesLengthRef = useRef(0);
    const prevLastMessageIdRef = useRef<string | null>(null);
    const wasNearBottomRef = useRef(true);

    useEffect(() => {
        if (chatId === prevChatIdRef.current) return;
        prevChatIdRef.current = chatId;
        isLoadingRef.current = false;
        hasInitiallyScrolledRef.current = false;
        prevMessagesLengthRef.current = 0;
        prevLastMessageIdRef.current = null;
        wasNearBottomRef.current = true;
    }, [chatId]);

    if (scrollContainerRef.current) {
        const el = scrollContainerRef.current;
        wasNearBottomRef.current =
            el.scrollHeight - el.scrollTop - el.clientHeight < BOTTOM_THRESHOLD_PX;
    }

    useLayoutEffect(() => {
        const el = scrollContainerRef.current;
        if (!el) return;

        if (!hasInitiallyScrolledRef.current) {
            if (messages.length === 0) return;
            el.scrollTop = el.scrollHeight;
            hasInitiallyScrolledRef.current = true;
            prevMessagesLengthRef.current = messages.length;
            prevLastMessageIdRef.current = messages[messages.length - 1]?.id ?? null;
            return;
        }

        const lastMessage = messages[messages.length - 1];
        const isAppendedAtEnd =
            messages.length > prevMessagesLengthRef.current &&
            lastMessage?.id !== prevLastMessageIdRef.current;

        if (isAppendedAtEnd) {
            if (lastMessage?.isOwn || wasNearBottomRef.current) {
                el.scrollTop = el.scrollHeight;
            }
        }

        prevMessagesLengthRef.current = messages.length;
        prevLastMessageIdRef.current = lastMessage?.id ?? null;
    }, [messages]);

    useEffect(() => {
        const sentinel = sentinelRef.current;
        const root = scrollContainerRef.current;
        if (!sentinel || !root || !hasMore) return;

        const observer = new IntersectionObserver(
            (entries) => {
                if (!entries[0].isIntersecting) return;
                if (isLoadingRef.current || isLoadingOlder) return;
                if (!hasInitiallyScrolledRef.current) return;
                isLoadingRef.current = true;

                const container = scrollContainerRef.current;
                if (!container) return;

                const scrollHeightBefore = container.scrollHeight;
                const scrollTopBefore = container.scrollTop;

                Promise.resolve(onLoadMore()).then(() => {
                    requestAnimationFrame(() => {
                        const c = scrollContainerRef.current;
                        if (!c) return;
                        const scrollHeightAfter = c.scrollHeight;
                        c.scrollTop = scrollTopBefore + (scrollHeightAfter - scrollHeightBefore);
                        isLoadingRef.current = false;
                        prevMessagesLengthRef.current = messagesRefLenSafe();
                    });
                });

                function messagesRefLenSafe() {
                    return scrollContainerRef.current
                        ? Array.from(scrollContainerRef.current.querySelectorAll('[data-message-id]')).length
                        : prevMessagesLengthRef.current;
                }
            },
            { root, rootMargin: '400px 0px 0px 0px', threshold: 0 }
        )

        observer.observe(sentinel);
        return () => observer.disconnect();
    }, [hasMore, isLoadingOlder, onLoadMore, chatId]);

    return (
        <div className={styles.messagesAreaWrapper} ref={scrollContainerRef}>
            <div className={styles.messagesArea}>
                {hasMore && <div ref={sentinelRef} style={{ height: 1 }} />}
                {isLoadingOlder && (
                    <div className={styles.messageRow}>
                        <span>Загрузка истории…</span>
                    </div>
                )}
                {messages.map((message) => (
                    <div
                        key={message.id}
                        data-message-id={message.id}
                        className={`${styles.messageRow} ${message.isOwn ? styles.own : styles.other}`}
                    >
                        <div className={`${styles.bubble} ${message.isOwn ? styles.bubbleOwn : styles.bubbleOther}`}>
                            {message.content}
                            <span className={styles.bubbleTime}>{message.time}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}

export default MessagesList;
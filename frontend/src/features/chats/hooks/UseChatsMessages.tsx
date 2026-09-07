import { useState } from 'react'
import { getChatMessagesRequest } from '../../chats/api/messages'
import { mapMessage } from '../utils/mapMessages'
import type { BackendMessage, ChatMessageUI } from '../../chats/types'

export function useChatMessages(currentUserId: number | null) {
    const [messagesByChat, setMessagesByChat] = useState<Record<string, ChatMessageUI[]>>({});
    const [loadedHistoryFor, setLoadedHistoryFor] = useState<Set<string>>(new Set());

    const [nextBeforeMessageId, setNextBeforeMessageId] = useState<Record<string, number | null>>({});
    const [loadingOlderFor, setLoadingOlderFor] = useState<Set<string>>(new Set());
 
    const loadHistory = (chatId: string) => {
        if (loadedHistoryFor.has(chatId)) return;
 
        getChatMessagesRequest(Number(chatId)).then((res) => {
            const uiMessages = [...res.items].reverse().map((m) => mapMessage(m, currentUserId));
 
            setMessagesByChat((prev) => ({ ...prev, [chatId]: uiMessages }));
            setLoadedHistoryFor((prev) => new Set(prev).add(chatId));
            setNextBeforeMessageId((prev) => ({ ...prev, [chatId]: res.nextBeforeMessageId }));
        });
    };

    const loadOlderMessages = async (chatId: string) => {
        const cursor = nextBeforeMessageId[chatId];
 
        if (cursor == null) return;
        if (loadingOlderFor.has(chatId)) return;
 
        setLoadingOlderFor((prev) => new Set(prev).add(chatId));
 
        try {
            const res = await getChatMessagesRequest(Number(chatId), cursor);
            const olderMessages = [...res.items].reverse().map((m) => mapMessage(m, currentUserId));
 
            setMessagesByChat((prev) => ({
                ...prev,
                [chatId]: [...olderMessages, ...(prev[chatId] ?? [])],
            }));
            setNextBeforeMessageId((prev) => ({ ...prev, [chatId]: res.nextBeforeMessageId }));
        } finally {
            setLoadingOlderFor((prev) => {
                const next = new Set(prev);
                next.delete(chatId);
                return next;
            });
        }
    }

    const hasMoreHistory = (chatId: string) => nextBeforeMessageId[chatId] != null;
    const isLoadingOlder = (chatId: string) => loadingOlderFor.has(chatId);

    const appendMessage = (chatId: string, message: BackendMessage) => {
        const uiMessage = mapMessage(message, currentUserId);

        setMessagesByChat((prev) => ({
            ...prev,
            [chatId]: [...(prev[chatId] ?? []), uiMessage],
        }));

        return uiMessage;
    };

    const replaceMessage = (chatId: string, message: BackendMessage) => {
        const uiMessage = mapMessage(message, currentUserId);

        setMessagesByChat((prev) => ({
            ...prev,
            [chatId]: (prev[chatId] ?? []).map((m) => (m.id === uiMessage.id ? uiMessage : m)),
        }));
    };

    const removeMessage = (chatId: string, messageId: number) => {
        setMessagesByChat((prev) => ({
            ...prev,
            [chatId]: (prev[chatId] ?? []).filter((m) => m.id !== String(messageId)),
        }));
    };

    return {
        messagesByChat,
        loadHistory,
        loadOlderMessages,
        hasMoreHistory,
        isLoadingOlder,
        appendMessage,
        replaceMessage,
        removeMessage,
    }
}
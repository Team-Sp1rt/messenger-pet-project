import { useState } from 'react'
import { getChatMessagesRequest } from '../../chats/api/messages'
import { mapMessage } from '../utils/mapMessages'
import type { BackendMessage, ChatMessageUI } from '../../chats/types'

export function useChatMessages(currentUserId: number | null) {
    const [messagesByChat, setMessagesByChat] = useState<Record<string, ChatMessageUI[]>>({});
    const [loadedHistoryFor, setLoadedHistoryFor] = useState<Set<string>>(new Set());

    const loadHistory = (chatId: string) => {
        if (loadedHistoryFor.has(chatId)) return;

        getChatMessagesRequest(Number(chatId)).then((res) => {
            const uiMessages = [...res.items].reverse().map((m) => mapMessage(m, currentUserId));

            setMessagesByChat((prev) => ({ ...prev, [chatId]: uiMessages }));
            setLoadedHistoryFor((prev) => new Set(prev).add(chatId));
        });
    }

    const appendMessage = (chatId: string, message: BackendMessage) => {
        const uiMessage = mapMessage(message, currentUserId);

        setMessagesByChat((prev) => ({
            ...prev,
            [chatId]: [...(prev[chatId] ?? []), uiMessage],
        }));

        return uiMessage;
    }

    const replaceMessage = (chatId: string, message: BackendMessage) => {
        const uiMessage = mapMessage(message, currentUserId);

        setMessagesByChat((prev) => ({
            ...prev,
            [chatId]: (prev[chatId] ?? []).map((m) => (m.id === uiMessage.id ? uiMessage : m)),
        }));
    }

    const removeMessage = (chatId: string, messageId: number) => {
        setMessagesByChat((prev) => ({
            ...prev,
            [chatId]: (prev[chatId] ?? []).filter((m) => m.id !== String(messageId)),
        }));
    }

    return { messagesByChat, loadHistory, appendMessage, replaceMessage, removeMessage };
}
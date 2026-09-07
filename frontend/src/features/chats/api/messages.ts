import { apiFetch } from '../../../shared/api/Client'
import type { GetChatMessagesResponse } from '../typesWs'

export function getChatMessagesRequest(chatId: number, beforeMessageId?: number, limit = 30) {
    const params = new URLSearchParams({ limit: String(limit) });

    if (beforeMessageId !== undefined) {
        params.set('beforeMessageId', String(beforeMessageId));
    }

    return apiFetch<GetChatMessagesResponse>(`/chats/${chatId}/messages?${params.toString()}`);
}
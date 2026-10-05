import type { ChatSummaryDto, ChatDto, Chat, BackendMessage } from '../types'

export function lastMessageFields(msg: BackendMessage | null | undefined) {
    return msg?.createdAt
        ? {
            lastMessage: msg.content ?? '',
            time: formatChatTime(msg.createdAt),
            lastMessageAt: msg.createdAt,
            lastMessageId: String(msg.id),
        }
        : { lastMessage: '', time: '', lastMessageAt: null, lastMessageId: null };
}

export function mapChatSummary(dto: ChatSummaryDto, currentUserId: number | null): Chat {
    const other = dto.members.find(m => m.id !== currentUserId) ?? dto.members[0];

    return {
        id: String(dto.id),
        name: other.username,
        ...lastMessageFields(dto.lastMessage),
    }
}

export function mapNewChat(dto: ChatDto, currentUserId: number | null): Chat {
    const other = dto.members.find(m => m.id !== currentUserId) ?? dto.members[0];

    return {
        id: String(dto.id),
        name: other.username,
        ...lastMessageFields(null),
    }
}

function formatChatTime(iso: string) {
    const date = new Date(iso);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();

    return isToday
        ? date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        : date.toLocaleDateString([], { day: '2-digit', month: '2-digit' })
}
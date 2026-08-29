import type { ChatSummaryDto, ChatDto, Chat } from '../types'

export function mapChatSummary(dto: ChatSummaryDto, currentUserId: number | null): Chat {
    const other = dto.members.find(m => m.id !== currentUserId) ?? dto.members[0]

    return {
        id: String(dto.id),
        name: other.username,
        lastMessage: dto.lastMessage?.content ?? '',
        time: dto.lastMessage ? formatChatTime(dto.lastMessage.createdAt) : '',
    }
}

export function mapNewChat(dto: ChatDto, currentUserId: number | null): Chat {
    const other = dto.members.find(m => m.id !== currentUserId) ?? dto.members[0]

    return {
        id: String(dto.id),
        name: other.username,
        lastMessage: '',
        time: '',
    }
}

function formatChatTime(iso: string) {
    const date = new Date(iso)
    const now = new Date()
    const isToday = date.toDateString() === now.toDateString()

    return isToday
        ? date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        : date.toLocaleDateString([], { day: '2-digit', month: '2-digit' })
}
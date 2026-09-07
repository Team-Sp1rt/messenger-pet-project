import type { BackendMessage, ChatMessageUI } from '../types'

export function mapMessage(message: BackendMessage, currentUserId: number | null): ChatMessageUI {
    return {
        id: String(message.id),
        content: message.content,
        time: new Date(message.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        isOwn: message.userId === currentUserId,
    }
}
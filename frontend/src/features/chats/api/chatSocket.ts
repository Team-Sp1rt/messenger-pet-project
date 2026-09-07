import { subscribeTopic, publishCommand } from '../../../shared/api/stompClient'
import type { ChatEvent, WebSocketErrorPayload } from '../typesWs'

export function subscribeChatEvents(chatId: number, token: string, onEvent: (event: ChatEvent) => void) {
    return subscribeTopic(`/topic/chats/${chatId}/events`, token, onEvent);
}

export function subscribeCommandErrors(token: string, onError: (error: WebSocketErrorPayload) => void) {
    return subscribeTopic('/user/queue/errors', token, onError);
}

export function sendChatMessage(chatId: number, content: string, token: string) {
    publishCommand(`/app/chats/${chatId}/messages`, token, { content });
}

export function editChatMessage(chatId: number, messageId: number, content: string, token: string) {
    publishCommand(`/app/chats/${chatId}/messages/${messageId}/edit`, token, { content });
}

export function deleteChatMessage(chatId: number, messageId: number, token: string) {
    publishCommand(`/app/chats/${chatId}/messages/${messageId}/delete`, token);
}
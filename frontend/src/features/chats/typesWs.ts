import type { BackendMessage } from "./types"

export interface WSMessageCreatedEvent {
    type: 'MESSAGE_CREATED';
    message: BackendMessage;
}

export interface WSMessageUpdatedEvent {
    type: 'MESSAGE_UPDATED';
    message: BackendMessage;
}

export interface WSMessageDeletedEvent {
    type: 'MESSAGE_DELETED';
    chatId: number;
    messageId: number;
}

export type ChatEvent = WSMessageCreatedEvent | WSMessageUpdatedEvent | WSMessageDeletedEvent;

export interface WebSocketErrorPayload {
    code: string;
    message: string;
}

export interface GetChatMessagesResponse {
    items: BackendMessage[];
    nextBeforeMessageId: number | null;
}
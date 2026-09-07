import type { WebSocketErrorPayload } from '../typesWs'

export function friendlyErrorMessage(code: WebSocketErrorPayload['code']): string {
    switch (code) {
        case 'CHAT_NOT_FOUND': return 'This chat no longer exists'
        case 'CHAT_ACCESS_DENIED': return 'You are no longer in this chat'
        case 'MESSAGE_NOT_FOUND': return 'Message not found'
        case 'MESSAGE_ACCESS_DENIED': return 'You can only edit your own messages'
        case 'MESSAGE_INVALID_CONTENT': return 'The message failed validation — please check the text'
        case 'MESSAGE_OPERATION_FAILED': return 'The operation could not be completed; please try again'
        default: return 'An error occurred'
    }
}
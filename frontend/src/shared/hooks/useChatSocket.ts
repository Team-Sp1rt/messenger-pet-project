import { useEffect, useRef } from 'react'
import type { StompSubscription } from '@stomp/stompjs'
import { connectStomp, disconnectStomp } from '../../shared/api/stompClient'
import { subscribeChatEvents, subscribeCommandErrors, sendChatMessage } from '../../features/chats/api/chatSocket'
import type { ChatEvent, WebSocketErrorPayload } from '../../features/chats/typesWs'

interface UseChatSocketParams {
    token: string | null,
    chatIds: number[],
    onEvent: (chatId: number, event: ChatEvent) => void,
    onCommandError: (error: WebSocketErrorPayload) => void
}

export function useChatSocket({ token, chatIds, onEvent, onCommandError }: UseChatSocketParams) {
    const subscriptionsRef = useRef<Map<number, StompSubscription>>(new Map());
    const chatIdsRef = useRef<number[]>(chatIds);
    const tokenRef = useRef<string | null>(token);

    const trySubscribeAll = () => {
        const t = tokenRef.current;
        if (!t) return;

        chatIdsRef.current.forEach((chatId) => {
            if (subscriptionsRef.current.has(chatId)) return;
            
            const sub = subscribeChatEvents(chatId, t, (event) => onEvent(chatId, event));

            if (sub) subscriptionsRef.current.set(chatId, sub);
        })
    }

    useEffect(() => {
        chatIdsRef.current = chatIds;
        trySubscribeAll();
    }, [chatIds])

    useEffect(() => {
        tokenRef.current = token;
        if (!token) return;

        connectStomp(
            token,
            () => {
                subscribeCommandErrors(token, onCommandError);
                trySubscribeAll();
            },
            (message) => {
                // TODO: протухший/невалидный токен здесь тоже прилетит как STOMP ERROR —
                // можно завести отдельный колбэк и дёргать logout() из AuthProvider
                console.error('STOMP fatal error:', message);
            }
        )

        return () => {
            subscriptionsRef.current.forEach((sub) => sub.unsubscribe());
            subscriptionsRef.current.clear();
            disconnectStomp();
        }
    }, [token])

    const send = (chatId: number, content: string) => {
        if (!tokenRef.current) return
        sendChatMessage(chatId, content, tokenRef.current)
    }

    return { sendMessage: send }
}
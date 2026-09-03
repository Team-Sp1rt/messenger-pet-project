import { useEffect, useRef } from 'react'
import type { StompSubscription } from '@stomp/stompjs'
import { connectStomp, disconnectStomp } from '../../shared/api/stompClient'
import { subscribeChatEvents, subscribeCommandErrors, sendChatMessage, editChatMessage, deleteChatMessage } from '../../features/chats/api/chatSocket'
import type { ChatEvent, WebSocketErrorPayload } from '../../features/chats/typesWs'
import { isTokenExpired } from '../utils/jwt'

interface UseChatSocketParams {
    token: string | null,
    chatIds: number[],
    onEvent: (chatId: number, event: ChatEvent) => void,
    onCommandError: (error: WebSocketErrorPayload) => void,
    onFatalError: (message: string, isAuthError: boolean) => void
}

export function useChatSocket({ token, chatIds, onEvent, onCommandError, onFatalError }: UseChatSocketParams) {
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
    };

    const pruneStaleSubscriptions = () => {
        const activeIds = new Set(chatIdsRef.current);
 
        subscriptionsRef.current.forEach((sub, chatId) => {
            if (!activeIds.has(chatId)) {
                sub.unsubscribe();
                subscriptionsRef.current.delete(chatId);
            }
        })
    };

    useEffect(() => {
        chatIdsRef.current = chatIds;
        pruneStaleSubscriptions();
        trySubscribeAll();
    }, [chatIds])

    useEffect(() => {
        tokenRef.current = token;
        if (!token) return;
 
        connectStomp(
            () => tokenRef.current,
            () => {
                const t = tokenRef.current;
                if (!t) return;
                subscribeCommandErrors(t, onCommandError);
                trySubscribeAll();
            },
            (message) => {
                const authRelated = isTokenExpired(tokenRef.current);
                onFatalError(message, authRelated);
            }
        )
 
        return () => {
            subscriptionsRef.current.forEach((sub) => sub.unsubscribe());
            subscriptionsRef.current.clear();
            disconnectStomp();
        }
    }, [token]);

    const send = (chatId: number, content: string) => {
        const t = tokenRef.current;
        if (!t || isTokenExpired(t)) return false;
 
        sendChatMessage(chatId, content, t);
        return true;
    }

    const edit = (chatId: number, messageId: number, content: string) => {
        const t = tokenRef.current;
        if (!t || isTokenExpired(t)) return false;

        editChatMessage(chatId, messageId, content, t);
        return true;
    }

    const remove = (chatId: number, messageId: number) => {
        const t = tokenRef.current;
        if (!t || isTokenExpired(t)) return false;

        deleteChatMessage(chatId, messageId, t);
        return true;
    }

    return { sendMessage: send, editMessage: edit, deleteMessage: remove };
}
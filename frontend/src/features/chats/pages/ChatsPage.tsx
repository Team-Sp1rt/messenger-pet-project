import { useEffect, useMemo, useState } from 'react'

import styles from '../styles/Chats.module.css'

import { getChatsRequest, createChatRequest } from '../../chats/api/chats'
import { getChatMessagesRequest } from '../../chats/api/messages'

import type { ChatEvent } from '../typesWs'
import type { Chat, ChatMessageUI, UserSummary } from '../../chats/types'

import { mapChatSummary, mapNewChat } from '../utils/mapChat'
import { mapMessage } from '../utils/mapMessages'

import { useChatSocket } from '../../../shared/hooks/useChatSocket'

import { useAuth } from '../../../shared/context/AuthContext'
import ChatsHeader from '../components/ChatsHeader'
import ListChats from '../components/leftColumn/ListChats'
import ChatWindow from '../components/mainColumn/ChatWindow'

function ChatsPage() {
    const { token, userId: currentUserId } = useAuth();

    const [chats, setChats] = useState<Chat[]>([]);
    const [chatMembers, setChatMembers] = useState<Record<string, number>>({});
    const [activeChatId, setActiveChatId] = useState<string | null>(null);
    const [messagesByChat, setMessagesByChat] = useState<Record<string, ChatMessageUI[]>>({});
    const [loadedHistoryFor, setLoadedHistoryFor] = useState<Set<string>>(new Set());

    useEffect(() => {
        getChatsRequest().then((res) => {
            setChats(res.items.map((dto) => mapChatSummary(dto, currentUserId)));

            const membersMap: Record<string, number> = {}
            res.items.forEach((dto) => {
                const other = dto.members.find((m) => m.id !== currentUserId) ?? dto.members[0];
                membersMap[String(dto.id)] = other.id;
            })
            
            setChatMembers(membersMap);
        })
    }, [currentUserId])

    const handleChatEvent = (chatId: number, event: ChatEvent) => {
        const chatKey = String(chatId);

        if (event.type === 'MESSAGE_CREATED') {
            const uiMessage = mapMessage(event.message, currentUserId);

            setMessagesByChat((prev) => ({
                ...prev,
                [chatKey]: [...(prev[chatKey] ?? []), uiMessage],
            }));

            setChats((prev) => {
                const updated = prev.map((chat) =>
                    chat.id === chatKey
                        ? { ...chat, lastMessage: event.message.content, time: uiMessage.time, lastMessageAt: event.message.createdAt }
                        : chat
                );
                
                return [...updated].sort((a, b) => {
                    if (!a.lastMessageAt) return 1
                    if (!b.lastMessageAt) return -1
                    return new Date(b.lastMessageAt).getTime() - new Date(a.lastMessageAt).getTime()
                });
            });
        }

        if (event.type === 'MESSAGE_UPDATED') {
            const uiMessage = mapMessage(event.message, currentUserId);

            setMessagesByChat((prev) => ({
                ...prev,
                [chatKey]: (prev[chatKey] ?? []).map((m) => (m.id === uiMessage.id ? uiMessage : m)),
            }));
        }

        if (event.type === 'MESSAGE_DELETED') {
            setMessagesByChat((prev) => ({
                ...prev,
                [chatKey]: (prev[chatKey] ?? []).filter((m) => m.id !== String(event.messageId)),
            }));
        }
    }

    const handleCommandError = (error: { code: string; message: string }) => {
        // TODO: показать тост/уведомление вместо console.error
        console.error('Chat command error:', error.code, error.message);
    }

    const chatIds = useMemo(() => 
        chats.map((c) => Number(c.id))
    , [chats]);

    const { sendMessage } = useChatSocket({
        token,
        chatIds,
        onEvent: handleChatEvent,
        onCommandError: handleCommandError,
    })

    const openChat = (chatId: string) => {
        setActiveChatId(chatId);

        if (loadedHistoryFor.has(chatId)) return;

        getChatMessagesRequest(Number(chatId)).then((res) => {
            const uiMessages = [...res.items].reverse().map((m) => mapMessage(m, currentUserId));

            setMessagesByChat((prev) => ({ ...prev, [chatId]: uiMessages }));
            setLoadedHistoryFor((prev) => new Set(prev).add(chatId));
        })
    }

    const handleSelectUser = async (user: UserSummary) => {
        const existingChatId = Object.entries(chatMembers).find(
            ([, memberId]) => memberId === user.id
        )?.[0];

        if (existingChatId) {
            openChat(existingChatId);
            return;
        }

        const created = await createChatRequest({ memberId: user.id });
        const chatId = String(created.id);

        setChats((prev) => [mapNewChat(created, currentUserId), ...prev]);
        setChatMembers((prev) => ({ ...prev, [chatId]: user.id }));
        openChat(chatId);
    }

    const handleSendMessage = (content: string) => {
        if (!activeChatId) return;

        sendMessage(Number(activeChatId), content);
    }

    const activeChat = chats.find((c) => c.id === activeChatId) ?? null;

    return (
        <div className={styles.bodyWrapper}>
            <div className={styles.body} />

            <div className={styles.leftColumnMainWrapper}>
                <div className={styles.leftColumnMain}>
                    <ChatsHeader onSelectUser={handleSelectUser} />
                    <ListChats chats={chats} activeChatId={activeChatId} onSelectChat={openChat} />
                </div>
            </div>

            <div className={styles.chatWindowWrapper}>
                <ChatWindow
                    chat={activeChat}
                    messages={activeChatId ? messagesByChat[activeChatId] ?? [] : []}
                    onSend={handleSendMessage}
                />
            </div>
        </div>
    )
}

export default ChatsPage;
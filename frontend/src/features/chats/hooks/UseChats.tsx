import { useCallback, useEffect, useMemo, useState } from 'react'
import { getChatsRequest, createChatRequest } from '../api/chats.ts'
import { lastMessageFields, mapChatSummary, mapNewChat } from '../utils/mapChat'
import type { BackendMessage, Chat, ChatSummaryDto, UserSummary } from '../types.ts'

interface UseChatsParams {
    currentUserId: number | null
}

const sortChats = (list: Chat[]) =>
    [...list].sort((a, b) => {
        if (!a.lastMessageAt) return 1;
        if (!b.lastMessageAt) return -1;
        return new Date(b.lastMessageAt).getTime() - new Date(a.lastMessageAt).getTime();
    });

export function useChats({ currentUserId }: UseChatsParams) {
    const [chats, setChats] = useState<Chat[]>([]);
    const [chatMembers, setChatMembers] = useState<Record<string, number>>({});

    const applyChats = useCallback((items: ChatSummaryDto[]) => {
        setChats(items.map((dto) => mapChatSummary(dto, currentUserId)));

        const membersMap: Record<string, number> = {};
        items.forEach((dto) => {
            const other = dto.members.find((m) => m.id !== currentUserId) ?? dto.members[0];
            membersMap[String(dto.id)] = other.id;
        });
        setChatMembers(membersMap);
    }, [currentUserId]);

    const reloadChats = useCallback(async () => {
        const res = await getChatsRequest();
        applyChats(res.items);
    }, [applyChats]);

    useEffect(() => {
        let cancelled = false;

        getChatsRequest()
            .then((res) => {
                if (!cancelled) applyChats(res.items);
            })
            .catch((e) => console.error('Failed to load chats', e));

        return () => { cancelled = true; };
    }, [applyChats]);

    const chatIds = useMemo(() => chats.map((c) => Number(c.id)), [chats]);

    const findChatIdByMember = (userId: number) =>
        Object.entries(chatMembers).find(([, memberId]) => memberId === userId)?.[0];

    const createChatWithUser = async (user: UserSummary) => {
        const created = await createChatRequest({ memberId: user.id });
        const chatId = String(created.id);

        setChats((prev) => [mapNewChat(created, currentUserId), ...prev]);
        setChatMembers((prev) => ({ ...prev, [chatId]: user.id }));

        return chatId;
    };

    const setLastMessage = (chatId: string, msg: BackendMessage | null) =>
        setChats((prev) =>
            sortChats(prev.map((c) => (c.id === chatId ? { ...c, ...lastMessageFields(msg) } : c)))
        );

    const applyMessageEdited = (msg: BackendMessage) =>
        setChats((prev) =>
            prev.map((c) =>
                c.id === String(msg.chatId) && c.lastMessageId === String(msg.id)
                    ? { ...c, lastMessage: msg.content }
                    : c
            )
        );

    return {
        chats,
        chatIds,
        chatMembers,
        findChatIdByMember,
        createChatWithUser,
        setLastMessage,
        applyMessageEdited,
        reloadChats,
    };
}
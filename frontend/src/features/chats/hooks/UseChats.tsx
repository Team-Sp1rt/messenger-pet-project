import { useEffect, useMemo, useState } from 'react'
import { getChatsRequest, createChatRequest } from '../../chats/api/chats'
import { mapChatSummary, mapNewChat } from '../utils/mapChat'
import type { Chat, UserSummary } from '../../chats/types'

interface UseChatsParams {
    currentUserId: number | null
}

export function useChats({ currentUserId }: UseChatsParams) {
    const [chats, setChats] = useState<Chat[]>([]);
    const [chatMembers, setChatMembers] = useState<Record<string, number>>({});

    useEffect(() => {
        getChatsRequest().then((res) => {
            setChats(res.items.map((dto) => mapChatSummary(dto, currentUserId)));

            const membersMap: Record<string, number> = {};
            res.items.forEach((dto) => {
                const other = dto.members.find((m) => m.id !== currentUserId) ?? dto.members[0];
                membersMap[String(dto.id)] = other.id;
            });

            setChatMembers(membersMap);
        })
    }, [currentUserId]);

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

    const touchChatPreview = (chatId: string, content: string, time: string, createdAt: string) => {
        setChats((prev) => {
            const updated = prev.map((chat) =>
                chat.id === chatId
                    ? { ...chat, lastMessage: content, time, lastMessageAt: createdAt }
                    : chat
            );

            return [...updated].sort((a, b) => {
                if (!a.lastMessageAt) return 1;
                if (!b.lastMessageAt) return -1;
                return new Date(b.lastMessageAt).getTime() - new Date(a.lastMessageAt).getTime();
            });
        });
    };

    return { chats, chatIds, chatMembers, findChatIdByMember, createChatWithUser, touchChatPreview };
}
import { useEffect, useState } from 'react'
import ChatsHeader from '../components/ChatsHeader'
import ListChats from '../components/leftColumn/ListChats'
import ChatWindow from '../components/mainColumn/ChatWindow'
import type { Chat, ChatMessageUI, UserSummary } from '../types'
import styles from '../styles/Chats.module.css'
import { useAuth } from '../../../shared/context/AuthContext'
import { mapChatSummary, mapNewChat } from '../utils/mapChat'
import { createChatRequest, getChatsRequest } from '../api/chats'

function ChatsPage() {
    const [chats, setChats] = useState<Chat[]>([])
    const [chatMembers, setChatMembers] = useState<Record<string, number>>({});
    const [activeChatId, setActiveChatId] = useState<string | null>(null);
    const [messages, setMessages] = useState<Record<string, ChatMessageUI[]>>({});
    
    const { userId: currentUserId } = useAuth();

    useEffect(() => {
        getChatsRequest().then((res) => {
            setChats(res.items.map((dto) => mapChatSummary(dto, currentUserId)))

            const membersMap: Record<string, number> = {}
            res.items.forEach((dto) => {
                const other = dto.members.find((m) => m.id !== currentUserId) ?? dto.members[0]
                membersMap[String(dto.id)] = other.id
            })
            setChatMembers(membersMap)
        })
    }, [])

    const openChat = (chatId: string) => {
        setActiveChatId(chatId)
    }

    const handleSelectUser = async (user: UserSummary) => {
        const existingChatId = Object.entries(chatMembers).find(
            ([, memberId]) => memberId === user.id
        )?.[0]

        if (existingChatId) {
            openChat(existingChatId)
            return
        }

        const created = await createChatRequest({ memberId: user.id })
        const chatId = String(created.id)
        setChats((prev) => [mapNewChat(created, currentUserId), ...prev])
        setChatMembers((prev) => ({ ...prev, [chatId]: user.id }))
        openChat(chatId)
    }

    const handleSendMessage = (content: string) => {
        if (!activeChatId) return
        // TODO: заменить на publish в /app/chats/{chatId}/messages через STOMP,
        // добавление в список будет приходить обратно через MESSAGE_CREATED
        const localMessage: ChatMessageUI = {
            id: crypto.randomUUID(),
            content,
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            isOwn: true,
        }
        setMessages((prev) => ({
            ...prev,
            [activeChatId]: [...(prev[activeChatId] ?? []), localMessage],
        }))
    }

    const activeChat = chats.find((c) => c.id === activeChatId) ?? null

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
                    messages={activeChatId ? messages[activeChatId] ?? [] : []}
                    onSend={handleSendMessage}
                />
            </div>
        </div>
    )
}

export default ChatsPage;
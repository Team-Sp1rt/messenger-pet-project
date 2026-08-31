import { useState } from 'react'

import styles from '../styles/Chats.module.css'

import { useAuth } from '../../../shared/context/AuthContext'

import type { ChatEvent } from '../typesWs'
import type { UserSummary } from '../../chats/types'

import { useChatSocket } from '../../../shared/hooks/useChatSocket'
import { useNotice } from '../hooks/UseNotice'
import { useChatMessages } from '../hooks/UseChatsMessages'
import { useChats } from '../hooks/UseChats'

import ChatsHeader from '../components/ChatsHeader'
import ListChats from '../components/leftColumn/ListChats'
import ChatWindow from '../components/mainColumn/ChatWindow'
import NoticeBanner from '../components/NoticeBanner'

import { friendlyErrorMessage } from '../utils/errorMessages'

function ChatsPage() {
    const { token, userId: currentUserId, logout } = useAuth();

    const { chats, chatIds, findChatIdByMember, createChatWithUser, touchChatPreview } = useChats({ currentUserId });
    const { messagesByChat, loadHistory, appendMessage, replaceMessage, removeMessage } = useChatMessages(currentUserId);
    const { notice, notifyError, notifyInfo, dismiss } = useNotice();

    const [activeChatId, setActiveChatId] = useState<string | null>(null);

    const handleChatEvent = (chatId: number, event: ChatEvent) => {
        const chatKey = String(chatId);

        if (event.type === 'MESSAGE_CREATED') {
            const uiMessage = appendMessage(chatKey, event.message);
            touchChatPreview(chatKey, event.message.content, uiMessage.time, event.message.createdAt);
        }

        if (event.type === 'MESSAGE_UPDATED') {
            replaceMessage(chatKey, event.message);
        }

        if (event.type === 'MESSAGE_DELETED') {
            removeMessage(chatKey, event.messageId);
        }
    };

    const { sendMessage } = useChatSocket({
        token,
        chatIds,
        onEvent: handleChatEvent,
        onCommandError: (error) => notifyInfo(friendlyErrorMessage(error.code)),
        onFatalError: (message, isAuthError) => {
            notifyError(isAuthError ? 'Session expired; please log in again' : `Connection lost: ${message}`);
            logout();
        },
    });

    const openChat = (chatId: string) => {
        setActiveChatId(chatId);
        loadHistory(chatId);
    };

    const handleSelectUser = async (user: UserSummary) => {
        const existingChatId = findChatIdByMember(user.id);

        if (existingChatId) {
            openChat(existingChatId);
            return;
        }

        const chatId = await createChatWithUser(user);
        openChat(chatId);
    };

    const handleSendMessage = (content: string) => {
        if (!activeChatId) return;

        const sent = sendMessage(Number(activeChatId), content);

        if (!sent) {
            notifyError('Failed to send: session expired');
        }
    };

    const activeChat = chats.find((c) => c.id === activeChatId) ?? null;

    return (
        <div className={styles.bodyWrapper}>
            <div className={styles.body} />

            {notice && (
                <NoticeBanner message={notice.message} kind={notice.kind} onDismiss={dismiss} />
            )}

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
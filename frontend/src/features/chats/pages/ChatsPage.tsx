import { useState } from 'react'

import styles from '../styles/Chats.module.css'

import { useAuth } from '../../../shared/context/AuthContext'

import type { ChatEvent } from '../typesWs'
import type { ChatMessageUI, Folder, UserSummary } from '../../chats/types'

import { useChatSocket } from '../../../shared/hooks/useChatSocket'
import { useNotice } from '../hooks/UseNotice'
import { useChatMessages } from '../hooks/UseChatsMessages'
import { useChats } from '../hooks/UseChats'

import ChatsHeader from '../components/leftColumn/ChatsHeader'
import ListChats from '../components/leftColumn/ListChats'
import ChatWindow from '../components/mainColumn/ChatWindow'
import NoticeBanner from '../components/NoticeBanner'

import { friendlyErrorMessage } from '../utils/errorMessages'
import ListOfGlobalSearch from '../components/leftColumn/ListOfGlobalSearch'
import ChatFolders from '../components/leftColumn/ChatFolders'
import { useUserSearch } from '../hooks/UseUserSearch'

const folders: Folder[] = [
    { id: 'all', label: 'All' }
]

function ChatsPage() {
    const { token, userId: currentUserId, logout } = useAuth();

    const { chats, chatIds, findChatIdByMember, createChatWithUser, touchChatPreview } = useChats({ currentUserId });
    const { messagesByChat, loadHistory, loadOlderMessages,
        hasMoreHistory, isLoadingOlder, appendMessage, replaceMessage, removeMessage } = useChatMessages(currentUserId);
    const { query, setQuery, results, isSearching, reset } = useUserSearch(currentUserId);

    const { notice, notifyError, notifyInfo, dismiss } = useNotice();

    const [activeChatId, setActiveChatId] = useState<string | null>(null);
    const [activeFolder, setActiveFolder] = useState('all');
    const [isFocused, setIsFocused] = useState(false);

    const [editingMessage, setEditingMessage] = useState<ChatMessageUI | null>(null);

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

    const { sendMessage, editMessage, deleteMessage } = useChatSocket({
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

    const closeChat = () => {
        setActiveChatId(null);
    };

    const handleSelectUser = async (user: UserSummary) => {
        reset();
        setIsFocused(false);

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
 
        sendMessage(Number(activeChatId), content);
    }

    const handleEditMessage = (message: ChatMessageUI) => {
        setEditingMessage(message);
    };

    const handleSubmitEdit = (content: string) => {
        if (!activeChatId || !editingMessage) return;
        editMessage(Number(activeChatId), Number(editingMessage.id), content);
        setEditingMessage(null);
    };

    const handleCancelEdit = () => {
        setEditingMessage(null);
    };

    const handleDeleteMessage = (messageId: string) => {
        if (!activeChatId) return;
        deleteMessage(Number(activeChatId), Number(messageId));
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
                    <ChatsHeader
                        query={query}
                        onQueryChange={setQuery}
                        setIsFocused={setIsFocused}
                        isFocused={isFocused}
                        reset={reset}
                    />

                    {query === "" ?
                        <>
                            <ChatFolders
                                folders={folders}
                                activeFolderId={activeFolder}
                                onSelect={setActiveFolder}
                            />
                            <ListChats chats={chats} activeChatId={activeChatId} onSelectChat={openChat} />
                        </>
                        :
                        <ListOfGlobalSearch
                            query={query}
                            isSearching={isSearching}
                            results={results}
                            onSelectUser={handleSelectUser}
                        />
                    }
                </div>
            </div>

            <div className={styles.chatWindowWrapper}>
                <ChatWindow
                    chat={activeChat}
                    messages={activeChatId ? messagesByChat[activeChatId] ?? [] : []}
                    hasMoreHistory={activeChatId ? hasMoreHistory(activeChatId) : false}
                    isLoadingOlder={activeChatId ? isLoadingOlder(activeChatId) : false}
                    onLoadMore={() => activeChatId && loadOlderMessages(activeChatId)}
                    onSend={handleSendMessage}
                    onClose={closeChat}
                    onEditMessage={handleEditMessage}
                    onDeleteMessage={handleDeleteMessage}
                    editingMessage={editingMessage}
                    onSubmitEdit={handleSubmitEdit}
                    onCancelEdit={handleCancelEdit}
                />
            </div>
        </div>
    )
}

export default ChatsPage;
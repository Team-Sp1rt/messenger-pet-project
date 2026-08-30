export interface Folder {
    id: string;
    label: string;
    count?: number;
}

export interface Chat {
    id: string;
    name: string;
    lastMessage: string;
    time: string;
}

export interface SearchUsersPayload {
    username: string;
    limit?: number;
}

export interface SearchUsersResponse {
    items: UserSummary[];
}

export interface UserSummary {
    id: number;
    username: string;
}

export interface BackendMessage {
    id: number;
    chatId: number;
    userId: number;
    content: string;
    createdAt: string;
}

export interface ChatSummaryDto {
    id: number;
    members: UserSummary[];
    lastMessage: BackendMessage | null;
}

export interface ChatDto {
    id: number;
    members: UserSummary[];
}

export interface GetChatsResponse {
    items: ChatSummaryDto[];
}

export interface CreateChatPayload {
    memberId: number;
}

export interface ChatMessageUI {
    id: string;
    content: string;
    time: string;
    isOwn: boolean;
}
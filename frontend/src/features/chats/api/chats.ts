import { apiFetch } from "../../../shared/api/Client";
import type { ChatDto, CreateChatPayload, GetChatsResponse, SearchUsersPayload, SearchUsersResponse } from "../types";

export function searchUsersRequest(payload: SearchUsersPayload) {
    const params = new URLSearchParams({ username: payload.username });

    if (payload.limit !== undefined) {
        params.set('limit', String(payload.limit));
    }

    return apiFetch<SearchUsersResponse>(`/users/search?${params.toString()}`);
}

export function getChatsRequest() {
    return apiFetch<GetChatsResponse>('/chats');
}

export function createChatRequest(payload: CreateChatPayload) {
    return apiFetch<ChatDto>('/chats', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
}
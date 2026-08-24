CREATE INDEX idx_messages_chat_created_at_id
    ON messages (chat_id, created_at DESC, id DESC);

CREATE INDEX idx_chat_members_user_chat
    ON chat_members (user_id, chat_id);
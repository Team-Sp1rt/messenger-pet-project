CREATE TABLE chat_members (
    chat_id BIGINT,
    user_id BIGINT,
    FOREIGN KEY (chat_id) REFERENCES chats(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
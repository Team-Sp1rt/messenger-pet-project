INSERT INTO authorisation (login, password_hash)
VALUES
    ('creator-login', 'test-hash'),
    ('member-login', 'test-hash');

INSERT INTO users (id, username)
VALUES
    (1, 'creator'),
    (2, 'member');
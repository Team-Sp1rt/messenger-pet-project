CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    bio VARCHAR(50),
    birthday DATE,
    FOREIGN KEY (id) REFERENCES authorisation(id)
);
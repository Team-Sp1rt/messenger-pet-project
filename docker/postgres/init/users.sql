CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50),
    bio VARCHAR(50),
    birthday DATE
);

INSERT INTO users (username, BIRTHDAY)
VALUES ('Exam-play', '2006-10-19');

INSERT INTO users (username, BIRTHDAY)
VALUES ('HuaChenju', '2026-05-30');

INSERT INTO users (username, BIRTHDAY)
VALUES ('sonychello', '2006-01-30');

INSERT INTO users (username, BIRTHDAY)
VALUES ('InsomniaDemon', '2006-04-13');

SELECT * FROM users
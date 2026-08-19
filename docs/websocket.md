# WebSocket API

## Протокол и подключение

- Endpoint: `/ws`
- Префикс команд клиента: `/app`
- Префикс событий сервера: `/topic`
- Персональные ошибки: `/user/queue/errors`

При локальном запуске полный адрес подключения:

```text
ws://localhost:8080/ws
```

JWT передаётся при выполнении STOMP `CONNECT`:

```text
Authorization: Bearer <token>
```

JWT передаётся не в теле сообщения, а один раз при установке STOMP-соединения.

## Подписка на события чата

После открытия чата клиент подписывается на:

```text
/topic/chats/{chatId}/events
```

Все события имеют поле `type`, по которому клиент определяет необходимое
изменение интерфейса.

## Создание сообщения

Клиент отправляет команду в:

```text
/app/chats/{chatId}/messages
```

тело:

```json
{
  "content": "Привет"
}
```

После проверок сервер публикует `MESSAGE_CREATED` всем подписчикам чата.

Событие:

```json
{
  "type": "MESSAGE_CREATED",
  "message": {
    "id": "781",
    "chatId": "42",
    "userId": "15",
    "content": "Привет",
    "createdAt": "2026-08-19T12:30:45Z"
  }
}
```

## Редактирование сообщения

Клиент отправляет команду в:

```text
/app/chats/{chatId}/messages/{messageId}/edit
```

Тело:

```json
{
  "content": "Исправленный текст"
}
```


После сохранения сервер публикует:

```json
{
  "type": "MESSAGE_UPDATED",
  "message": {
    "id": "781",
    "chatId": "42",
    "userId": "15",
    "content": "Исправленный текст",
    "createdAt": "2026-08-19T12:30:45Z"
  }
}
```

## Удаление сообщения

Клиент отправляет команду без тела в:

```text
/app/chats/{chatId}/messages/{messageId}/delete
```


После удаления сервер публикует:

```json
{
  "type": "MESSAGE_DELETED",
  "chatId": "42",
  "messageId": "781"
}
```

## Ошибки

После подключения клиент подписывается на персональную очередь:

```text
/user/queue/errors
```

Формат ошибки:

```json
{
  "code": "MESSAGE_ACCESS_DENIED",
  "message": "You cannot modify this message"
}
```

Предусмотренные коды:

| Code | Описание                                             |
| --- |------------------------------------------------------|
| `UNAUTHORIZED` | JWT отсутствует, недействителен или истёк            |
| `CHAT_NOT_FOUND` | Чат не найден                                        |
| `CHAT_ACCESS_DENIED` | Пользователь не состоит в чате                       |
| `MESSAGE_NOT_FOUND` | Сообщение не найдено                                 |
| `MESSAGE_ACCESS_DENIED` | Пользователь не является автором сообщения           |
| `MESSAGE_INVALID_CONTENT` | Текст сообщения не прошёл валидацию (длина, пустота) |

## Краткая таблица

| Направление | Destination | Назначение                       |
| --- | --- |----------------------------------|
| Client to server | `/app/chats/{chatId}/messages` | Создать сообщение  MESSAGE_CREATED              |
| Client to server | `/app/chats/{chatId}/messages/{messageId}/edit` | Изменить сообщение  MESSAGE_UPDATED             |
| Client to server | `/app/chats/{chatId}/messages/{messageId}/delete` | Удалить сообщение  MESSAGE_DELETED              |
| Server to client | `/topic/chats/{chatId}/events` | События сообщений в чате         |
| Server to client | `/user/queue/errors` | Персональные ошибки пользователя |

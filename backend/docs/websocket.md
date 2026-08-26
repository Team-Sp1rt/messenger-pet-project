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

JWT передаётся в native-заголовке `Authorization` при выполнении STOMP-команд
`CONNECT`, `SEND` и `SUBSCRIBE`:

```text
Authorization: Bearer <token>
```

При `CONNECT` сервер проверяет JWT и закрепляет пользователя из claim `sub` за
WebSocket-сессией. При каждом последующем `SEND` и `SUBSCRIBE` сервер заново
проверяет переданный JWT и сравнивает его `sub` с пользователем сессии. Попытка
использовать токен другого пользователя отклоняется.

JWT передаётся в STOMP-заголовке, а не в JSON-теле команды.

## Подписка на события чата

После открытия чата клиент подписывается на:

```text
/topic/chats/{chatId}/events
```

Все события имеют поле `type`, по которому клиент определяет необходимое
изменение интерфейса.

Все идентификаторы (`id`, `chatId`, `userId`, `messageId`) передаются как
JSON-числа и соответствуют типу Java `Long` / OpenAPI `integer (int64)`.

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
    "id": 781,
    "chatId": 42,
    "userId": 15,
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
    "id": 781,
    "chatId": 42,
    "userId": 15,
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
  "chatId": 42,
  "messageId": 781
}
```

## Ошибки команд

После подключения клиент подписывается на персональную очередь ошибок:

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

В эту очередь приходят ошибки валидации и выполнения команд, обработанных через
`@MessageMapping`.

Предусмотренные коды персональных ошибок:

| Code | Описание                                             |
| --- |------------------------------------------------------|
| `CHAT_NOT_FOUND` | Чат не найден                                        |
| `CHAT_ACCESS_DENIED` | Пользователь не состоит в чате                       |
| `MESSAGE_NOT_FOUND` | Сообщение не найдено                                 |
| `MESSAGE_ACCESS_DENIED` | Пользователь не является автором сообщения           |
| `MESSAGE_INVALID_CONTENT` | Текст сообщения не прошёл валидацию (длина, пустота) |
| `MESSAGE_OPERATION_FAILED` | Операцию с сообщением не удалось выполнить            |

## Ошибки STOMP

Ошибки, возникшие в интерсепторах до вызова `@MessageMapping`, не отправляются
в `/user/queue/errors`. Сервер возвращает их как STOMP `ERROR`.

К таким ошибкам относятся:

- отсутствующий, недействительный или истёкший JWT;
- отсутствие аутентифицированной WebSocket-сессии;
- несовпадение `sub` текущего JWT с пользователем WebSocket-сессии;
- отправка команды в запрещённый destination;
- подписка на запрещённый destination;
- попытка подписаться на события чата, в котором пользователь не состоит.

Клиент должен отдельно обрабатывать STOMP `ERROR`. Такие ошибки не имеют формат
`WebSocketError` из персональной очереди и могут приводить к закрытию
WebSocket-соединения.

## Краткая таблица

| Направление | Destination | Назначение                       |
| --- | --- |----------------------------------|
| Client to server | `/app/chats/{chatId}/messages` | Создать сообщение  MESSAGE_CREATED              |
| Client to server | `/app/chats/{chatId}/messages/{messageId}/edit` | Изменить сообщение  MESSAGE_UPDATED             |
| Client to server | `/app/chats/{chatId}/messages/{messageId}/delete` | Удалить сообщение  MESSAGE_DELETED              |
| Server to client | `/topic/chats/{chatId}/events` | События сообщений в чате         |
| Server to client | `/user/queue/errors` | Персональные ошибки пользователя |

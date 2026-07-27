# Мессенджер

## 🏗️ Структура проекта

| Layer | Component | Technology | Description |
|-------|-----------|------------|-------------|
| **Frontend** | UI | React + Vite | Пользовательский интерфейс с компонентной архитектурой |
| | State | Context API | Управление глобальным состоянием |
| | API Client | Axios | HTTP-запросы к бэкенд-сервисам |
| | Routing | React Router | Навигация на стороне клиента |
| **Backend** | Auth Service | Java 21 + Spring Boot | Аутентификация и авторизация (JWT) |
| | Chat Service | Java 21 + Spring Boot | Обмен сообщениями в реальном времени через WebSockets |
| **Database** | PostgreSQL | SQL | Реляционная база данных для пользователей и сообщений |

### Backend модули
```text
backend/
├── auth-service/
│   └── src/
│       ├── main/
│       │   └── java/
│       │
│       └── test/
│           └── java/
│      
└── chat-service/
    └── src/
        ├── main/
        │   └── java/
        │
        └── test/
            └── java/
```
## Правила построения коммитов

### Компоненты

| Компонент | Описание |
|-----------|----------|
| `FRONTEND` | Vite + React frontend |
| `BACKEND` | Общие backend изменения |
| `DEVOPS` | Docker, настройка окружения |
| `DB` | Изменения в базе данных |
| `DOCS` | Документация |

### Типы изменений (для backend)

| Тип | Описание |
|-----|----------|
| `AUTH` | auth-service (микросервис авторизации) |
| `CHAT` | chat-service (микросервис чатов) |

### Примеры

```text
[FRONTEND] Добавлена страница логина
[BACKEND/AUTH] Реализована регистрация пользователей
[BACKEND/CHAT] Исправлена отправка сообщений
[FRONTEND] Обновлен дизайн чата
[DB] Добавлена таблица messages
[DOCS] Обновлен README.md
[BACKEND/AUTH+CHAT] Обновлена общая модель UserDTO

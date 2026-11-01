import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'

const WS_URL = `ws://${location.hostname}:8080/ws`;

let client: Client | null = null;

// Актуальные колбэки хранятся отдельно, чтобы повторный вызов connectStomp
// мог их обновить, не пересоздавая соединение.
let currentGetToken: () => string | null = () => null;
let currentOnConnect: () => void = () => {};
let currentOnFatalError: (message: string) => void = () => {};

export function connectStomp(
    getToken: () => string | null,
    onConnect: () => void,
    onFatalError: (message: string) => void
) {
    currentGetToken = getToken;
    currentOnConnect = onConnect;
    currentOnFatalError = onFatalError;

    // Клиент уже подключается или подключён: ничего не пересоздаём.
    if (client?.active) return client;

    const newClient = new Client({
        brokerURL: WS_URL,
        reconnectDelay: 3000,
        debug: (s) => console.debug('[stomp]', s),
        beforeConnect: () => {
            const token = currentGetToken();
            newClient.connectHeaders = { Authorization: `Bearer ${token ?? ''}` };
        },
        onConnect: () => currentOnConnect(),
        onStompError: (frame) => {
            // Ошибка авторизации и т.п.: останавливаем реконнекты, иначе будет цикл.
            const message = frame.headers['message'] ?? 'STOMP error';
            newClient.deactivate();
            if (client === newClient) client = null;
            currentOnFatalError(message);
        },
        onWebSocketError: (e) => console.error('[stomp] WS error', e),
        onWebSocketClose: (e) => console.warn('[stomp] WS close', e.code, e.reason),
    });

    client = newClient;
    newClient.activate();
    return newClient;
}

export function disconnectStomp() {
    const c = client;
    client = null;
    c?.deactivate();
}

export function subscribeTopic(
    destination: string,
    token: string,
    onMessage: (body: any) => void
): StompSubscription | undefined {
    if (!client || !client.connected) {
        console.warn('[stomp] subscribe skipped: not connected', destination);
        return undefined;
    }

    return client.subscribe(
        destination,
        (msg: IMessage) => {
            try {
                onMessage(JSON.parse(msg.body));
            } catch {
                // молча игнорю, не роняю подписку
            }
        },
        { Authorization: `Bearer ${token}` }
    );
}

export function publishCommand(destination: string, token: string, body?: unknown) {
    if (!client || !client.connected) {
        console.warn('[stomp] publish skipped: not connected', destination);
        return;
    }

    client.publish({
        destination,
        headers: { Authorization: `Bearer ${token}` },
        body: body !== undefined ? JSON.stringify(body) : '',
    });
}
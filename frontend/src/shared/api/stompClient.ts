import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'

const WS_URL = 'ws://localhost:8080/ws';

let client: Client | null = null;

export function connectStomp(
    getToken: () => string | null,
    onConnect: () => void,
    onFatalError: (message: string) => void
) {
    if (client) {
        client.deactivate();
    }

    client = new Client({
        brokerURL: WS_URL,
        beforeConnect: () => {
            const token = getToken();
            if (client) {
                client.connectHeaders = { Authorization: `Bearer ${token ?? ''}` };
            }
        },
        reconnectDelay: 3000,
        onConnect,
        onStompError: (frame) => {
            onFatalError(frame.headers['message'] ?? 'STOMP error');
        },
    })

    client.activate();
    return client;
}

export function disconnectStomp() {
    client?.deactivate();
    client = null;
}

export function subscribeTopic(
    destination: string,
    token: string,
    onMessage: (body: any) => void
): StompSubscription | undefined {
    if (!client || !client.connected) return undefined;

    return client.subscribe(
        destination,
        (msg: IMessage) => {
            try {
                onMessage(JSON.parse(msg.body))
            } catch {
                // молча игнорю, не роняю подписку
            }
        },
        { Authorization: `Bearer ${token}` }
    )
}

export function publishCommand(destination: string, token: string, body?: unknown) {
    if (!client || !client.connected) return

    client.publish({
        destination,
        headers: { Authorization: `Bearer ${token}` },
        body: body !== undefined ? JSON.stringify(body) : '',
    });
}
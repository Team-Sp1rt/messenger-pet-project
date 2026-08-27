export function isTokenExpired(token: string): boolean {
    const payload = token.split(".")[1];

    if (!payload) return true;

    try {
        const payloadBase64url = payload.replaceAll('-', '+').replaceAll('_', '/');
        const decodePayload = JSON.parse(window.atob(payloadBase64url));

        return decodePayload.exp < (Date.now() / 1000);
    } catch {
        return true;
    }
}
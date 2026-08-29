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

export function decodeJwtPayload(token: string): Record<string, any> | null {
    try {
        const payload = token.split('.')[1]
        const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
        const json = atob(normalized)
        return JSON.parse(json)
    } catch {
        return null
    }
}

export function getUserIdFromToken(token: string): number | null {
    const payload = decodeJwtPayload(token)
    return payload?.sub ? Number(payload.sub) : null
}
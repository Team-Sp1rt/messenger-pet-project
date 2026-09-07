import { useState } from 'react'

export interface Notice {
    message: string
    kind: 'error' | 'info'
}

export function useNotice() {
    const [notice, setNotice] = useState<Notice | null>(null);

    const notifyError = (message: string) => setNotice({ message, kind: 'error' });
    const notifyInfo = (message: string) => setNotice({ message, kind: 'info' });
    const dismiss = () => setNotice(null);

    return { notice, notifyError, notifyInfo, dismiss };
}
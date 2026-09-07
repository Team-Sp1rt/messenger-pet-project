import { useEffect, useRef, useState } from 'react'
import { searchUsersRequest } from '../api/chats'
import type { UserSummary } from '../types'

export function useUserSearch(currentUserId: number | null) {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState<UserSummary[]>([]);
    const [isSearching, setIsSearching] = useState(false);
    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        if (debounceRef.current) clearTimeout(debounceRef.current);

        const trimmed = query.trim();
        if (!trimmed) {
            setResults([]);
            setIsSearching(false);
            return;
        }

        setIsSearching(true);
        debounceRef.current = setTimeout(async () => {
            try {
                const res = await searchUsersRequest({ username: trimmed });
                const filterRes = res.items.filter((user) => String(user.id) !== String(currentUserId)).splice(0, 10);
                setResults(filterRes);
            } catch {
                setResults([]);
            } finally {
                setIsSearching(false);
            }
        }, 1000);

        return () => {
            if (debounceRef.current) clearTimeout(debounceRef.current);
        }
    }, [query, currentUserId]);

    const reset = () => {
        setQuery('');
        setResults([]);
    };

    return { query, setQuery, results, isSearching, reset };
}
export const useSessions = () => {
    const sessionLink = (conference: Conference, session: string): string => `/conference/${conference.id}/session/${session}`

    const findSession = (id: string, sessions?: Session[]): Session | undefined => {
        if (sessions === undefined) {
            return undefined;
        }

        return sessions.find((session) => {
            return session.id === id
        })
    }

    const duration = (session: Session): string | undefined => {
        if (session.length !== undefined) {
            const hours = `${Math.floor(session.length / 60)}`.padStart(2, "0");
            const minutes = `${session.length % 60}`.padStart(2, "0");

            return `${hours}:${minutes}`;
        }
    }

    return {
        sessionLink,
        findSession,
        duration
    }
}
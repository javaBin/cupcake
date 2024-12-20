export const useConferences = () => {
    const conferenceLink = (conference: Conference): string => `/conference/${conference.id}`

    const findConference = (id: string, conferences?: Conference[]): Conference | undefined => {
        if (conferences === undefined) {
            return undefined;
        }

        return conferences.find((conference) => {
            return conference.id === id
        })
    }

    const conferenceTitle = (conference?: Conference) => {
        if (conference !== undefined) {
            return conference.name;
        }

        return "Javazone";
    }

    return {
        conferenceLink,
        conferenceTitle,
        findConference
    }
}
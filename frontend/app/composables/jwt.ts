import type {User} from '@/types/user'

export const useJwt = () => {
    const parseJwt = (token: string): User | undefined => {
        const base64Payload = token.split('.')[1]

        if (base64Payload === undefined) {
            return undefined
        }

        const base64 = base64Payload.replace(/-/g, '+').replace(/_/g, '/')

        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        }).join(''))

        const jsonUser = JSON.parse(jsonPayload)

        return {
            id: jsonUser['slack_id'],
            name: jsonUser['name'],
            avatar: jsonUser['avatar'],
            email: jsonUser['email'],
        }
    }

    return {
        parseJwt
    }
}

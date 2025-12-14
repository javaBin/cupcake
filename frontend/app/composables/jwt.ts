import type { User } from "@/types/user"

export const useJwt = () => {
  const parseJwt = (token: string): User | undefined => {
    const base64Payload = token.split(".")[1]

    if (base64Payload === undefined) {
      return undefined
    }

    const base64 = base64Payload.replace(/-/g, "+").replace(/_/g, "/")

    let jsonPayload: string

    if (typeof window !== "undefined") {
      // Running in browser
      jsonPayload = decodeURIComponent(
        window
          .atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join(""),
      )
    } else {
      // Running in Node (SSR)
      const buffer = Buffer.from(base64, "base64")
      jsonPayload = buffer.toString("utf-8")
    }

    const jsonUser = JSON.parse(jsonPayload)

    return {
      id: jsonUser["slack_id"],
      name: jsonUser["name"],
      avatar: jsonUser["avatar"],
      email: jsonUser["email"],
    }
  }

  return {
    parseJwt,
  }
}

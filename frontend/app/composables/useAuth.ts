import type { User } from "@/types/user"

function parseJwtPayload(token: string): Record<string, unknown> | undefined {
  const base64Payload = token.split(".")[1]
  if (base64Payload === undefined) return undefined

  const base64 = base64Payload.replace(/-/g, "+").replace(/_/g, "/")

  let jsonPayload: string
  if (typeof window !== "undefined") {
    jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    )
  } else {
    jsonPayload = Buffer.from(base64, "base64").toString("utf-8")
  }

  return JSON.parse(jsonPayload)
}

let refreshPromise: Promise<void> | null = null

export const useAuth = () => {
  const idTokenCookie = useCookie("id_token", { readonly: true })
  const accessTokenCookie = useCookie("access_token", { readonly: true })

  const user = computed<User | undefined>(() => {
    const token = idTokenCookie.value
    if (!token) return undefined

    const payload = parseJwtPayload(token)
    if (!payload) return undefined

    return {
      id: payload["slack_id"] as string,
      name: payload["name"] as string,
      avatar: payload["avatar"] as string,
      email: payload["email"] as string,
    }
  })

  const isAuthenticated = computed(() => !!accessTokenCookie.value)

  const accessToken = computed(() => accessTokenCookie.value)

  const isAccessTokenExpiringSoon = () => {
    const token = accessTokenCookie.value
    if (!token) return true

    const payload = parseJwtPayload(token)
    if (!payload || typeof payload.exp !== "number") return true

    const expiresAt = payload.exp * 1000
    const twoMinutes = 2 * 60 * 1000
    return Date.now() + twoMinutes >= expiresAt
  }

  const refresh = async (): Promise<void> => {
    if (refreshPromise) return refreshPromise

    refreshPromise = $fetch("/refresh", { method: "POST" })
      .then(() => {})
      .catch(() => {})
      .finally(() => {
        refreshPromise = null
      })

    return refreshPromise
  }

  const logout = () => {
    const idCookie = useCookie("id_token")
    const accessCookie = useCookie("access_token")
    const refreshCookieWrite = useCookie("refresh_token")

    idCookie.value = null
    accessCookie.value = null
    refreshCookieWrite.value = null

    navigateTo("/")
  }

  return {
    user,
    isAuthenticated,
    accessToken,
    isAccessTokenExpiringSoon,
    refresh,
    logout,
  }
}

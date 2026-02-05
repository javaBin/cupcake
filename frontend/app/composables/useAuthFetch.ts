function withAuth(opts: Record<string, any> = {}): Record<string, any> {
  const { accessToken, isAccessTokenExpiringSoon, refresh } = useAuth()

  return {
    ...opts,
    async onRequest(context: { options: { headers: any } }) {
      if (import.meta.client && isAccessTokenExpiringSoon()) {
        await refresh()
      }

      const token = accessToken.value
      if (token) {
        context.options.headers = new Headers(context.options.headers)
        context.options.headers.set("Authorization", `Bearer ${token}`)
      }

      if (typeof opts.onRequest === "function") {
        await opts.onRequest(context)
      }
    },
    async onResponseError(context: { response: { status: number } }) {
      if (import.meta.client && context.response.status === 401) {
        await refresh()
      }

      if (typeof opts.onResponseError === "function") {
        await opts.onResponseError(context)
      }
    },
  }
}

export function useAuthFetch<T>(url: string, opts?: Record<string, any>) {
  return useFetch<T>(url, withAuth(opts) as any)
}

export function useAuthLazyFetch<T>(url: string, opts?: Record<string, any>) {
  return useLazyFetch<T>(url, withAuth(opts) as any)
}

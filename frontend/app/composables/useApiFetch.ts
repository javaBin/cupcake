import { ensureAccessToken } from './useAuth'

export class ApiError extends Error {
  public readonly status: number
  public readonly bodyText: string

  constructor(status: number, bodyText: string) {
    super(`API error: ${status}`)
    this.status = status
    this.bodyText = bodyText
  }
}

function buildHeaders(existing: HeadersInit | undefined, token: string | null): Headers {
  const h = new Headers(existing)
  if (token) h.set('Authorization', `Bearer ${token}`)
  return h
}

export async function apiFetch(
  input: RequestInfo | URL,
  init: RequestInit = {},
): Promise<Response> {
  const token = await ensureAccessToken(30)

  const res1 = await fetch(input, {
    ...init,
    headers: buildHeaders(init.headers, token),
  })

  if (res1.status !== 401) return res1

  const token2 = await ensureAccessToken(0)
  if (!token2) return res1

  return fetch(input, {
    ...init,
    headers: buildHeaders(init.headers, token2),
  })
}

export async function apiJson<T>(input: RequestInfo | URL, init: RequestInit = {}): Promise<T> {
  const res = await apiFetch(input, init)
  if (!res.ok) {
    const bodyText = await res.text().catch(() => '')
    throw new ApiError(res.status, bodyText)
  }
  return (await res.json()) as T
}

import { defineEventHandler, readRawBody, send, setResponseStatus } from "h3"

const backend =
  process.env.CUPCAKE_BACKEND ??
  (process.env.NODE_ENV === "development"
    ? "http://127.0.0.1:8080"
    : "https://cupcake-backend.java.no")

const shouldProxy = (url: string) =>
  url.startsWith("/api") ||
  url.startsWith("/login") ||
  url.startsWith("/slackCallback") ||
  url.startsWith("/refresh")

export default defineEventHandler(async (event) => {
  const url = event.node.req.url || ""
  if (url.startsWith("/api/_nuxt_icon")) return
  if (!shouldProxy(url)) return

  const targetUrl = backend + url
  const method = event.node.req.method || "GET"

  const headers: Record<string, string> = {}
  for (const [key, value] of Object.entries(event.node.req.headers)) {
    if (typeof value === "string") {
      headers[key] = value
    }
  }
  delete headers.host

  let body: string | undefined = undefined
  if (!["GET", "HEAD"].includes(method)) {
    body = (await readRawBody(event)) ?? undefined
  }

  const response = await fetch(targetUrl, {
    method,
    headers,
    body,
    redirect: "manual",
  })

  setResponseStatus(event, response.status)

  // Use getSetCookie() to correctly forward multiple Set-Cookie headers
  const setCookies = response.headers.getSetCookie()
  for (const cookie of setCookies) {
    event.node.res.appendHeader("set-cookie", cookie)
  }

  for (const [key, value] of response.headers.entries()) {
    if (
      key.toLowerCase() === "set-cookie" ||
      key.toLowerCase().startsWith("access-control-") ||
      key.toLowerCase() === "content-encoding"
    ) {
      continue
    }
    event.node.res.setHeader(key, value)
  }

  const data = await response.arrayBuffer()
  return send(event, Buffer.from(data))
})

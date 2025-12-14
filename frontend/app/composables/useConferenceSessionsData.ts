import type { Conference } from "@/types/conference"
import type { Session } from "@/types/session"

export function useConferenceSessionsData(conferenceId: string) {
  const user = useCookie("user_session", { readonly: true })

  const opts: UseFetchOptions = {}
  if (user.value) {
    opts.headers = { authorization: `Bearer ${user.value}` }
  }

  const conferencesFetch = useFetch<Conference[]>("/api/conferences", opts)
  const sessionsFetch = useLazyFetch<Session[]>(
    `/api/conferences/${conferenceId}/sessions`,
    opts,
  )

  const pending = computed(
    () =>
      conferencesFetch.status.value === "pending" ||
      sessionsFetch.status.value === "pending",
  )

  const ready = computed(
    () =>
      conferencesFetch.status.value === "success" &&
      sessionsFetch.status.value === "success",
  )

  return {
    opts,
    conferencesFetch,
    sessionsFetch,
    pending,
    ready,
  }
}

import type { User } from "@/types/user"
import { ApiError, apiJson } from "~/composables/useApiFetch"

export const useUser = () => {
  const user = useState<User | null>("user-data", () => null)
  const userPending = useState<boolean>("user-pending", () => false)
  const userError = useState<ApiError | null>("user-error", () => null)
  const userLoaded = useState<boolean>("user-loaded", () => false)

  const fetchUser = async (): Promise<void> => {
    if (userLoaded.value || userPending.value) return
    userPending.value = true
    userError.value = null
    try {
      user.value = await apiJson<User>("/api/me")
    } catch (e) {
      userError.value = e instanceof ApiError ? e : new ApiError(0, "Unknown error")
      user.value = null
    } finally {
      userPending.value = false
      userLoaded.value = true
    }
  }

  return { user, userPending, userError, userLoaded, fetchUser }
}

<script setup lang="ts">
const user = useCookie("user_session", {
  readonly: true,
})

const opts: UseFetchOptions = {}

if (user.value !== undefined) {
  opts["headers"] = {
    authorization: `Bearer ${user.value}`,
  }
}

const {
  data: conferences,
  pending,
  error,
} = await useFetch<Conference[]>(`/api/conferences`, opts)

const conferenceList = computed(() => conferences.value ?? [])
const conferenceCount = computed(() => conferenceList.value.length)

const { conferenceLink } = useConferences()

const conferenceItems = computed(() => [
  ...(error.value
    ? [[{ label: "Failed to load conferences", disabled: true }]]
    : []),

  conferenceList.value.map((c) => ({
    label: c.name,
    to: conferenceLink(c),
  })),

  ...(!pending.value && !error.value && conferenceCount.value === 0
    ? [[{ label: "No conferences found", disabled: true }]]
    : []),
])
</script>

<template>
  <UHeader title="Cupcake" to="/">
    <UDropdownMenu :items="conferenceItems" :content="{ align: 'start' }">
      <UButton
        variant="ghost"
        color="neutral"
        trailing-icon="i-lucide-chevron-down"
        :label="pending ? 'Loading…' : 'Select Conference'"
        :loading="pending"
        :disabled="pending || conferenceCount === 0"
        class="font-medium"
      />
    </UDropdownMenu>

    <template #right>
      <UColorModeButton variant="ghost" color="neutral" />
    </template>
  </UHeader>
</template>

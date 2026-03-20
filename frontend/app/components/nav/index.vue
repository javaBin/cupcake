<script setup lang="ts">
const {
  conferences,
  conferencesPending,
  conferencesError,
  conferencesLoaded,
  fetchConferences,
  conferenceLink,
} = useConferences()

await fetchConferences()

const route = useRoute()
const conferenceList = computed(() => conferences.value)
const conferenceCount = computed(() => conferenceList.value.length)

const currentConference = computed(() => {
  const id = route.params.conferenceId
  if (!id) return undefined
  return conferenceList.value.find((c) => c.id === id)
})

const buttonLabel = computed(() => {
  if (conferencesPending.value) return "Loading…"
  if (currentConference.value) return currentConference.value.name
  return "Select Conference"
})

const conferenceItems = computed(() => [
  ...(conferencesError.value
    ? [[{ label: "Failed to load conferences", disabled: true }]]
    : []),

  conferenceList.value.map((c) => ({
    label: c.name,
    to: conferenceLink(c),
  })),

  ...(conferencesLoaded.value && !conferencesError.value && conferenceCount.value === 0
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
        :label="buttonLabel"
        :loading="conferencesPending"
        :disabled="conferencesPending || conferenceCount === 0"
        class="font-medium"
      />
    </UDropdownMenu>

    <template #right>
      <UColorModeButton variant="ghost" color="neutral" />
    </template>
  </UHeader>
</template>

<script setup lang="ts">
import type { Session } from "@/types/session"
import type { Conference } from "@/types/conference"

const props = defineProps<{
  sessions: Session[]
  conference?: Conference
}>()

type Filters = {
  format?: string
  status?: string
  language?: string
  city?: string
  county?: string
}

const search = ref("")
const page = ref(1)
const pageSize = ref(50)

const filters = reactive<Filters>({
  format: undefined,
  status: undefined,
  language: undefined,
  city: undefined,
  county: undefined,
})

function sessionMatchesSearch(session: Session, q: string): boolean {
  const needle = q.toLowerCase().trim()
  if (!needle) return true

  const text = [
    session.title ?? "",
    session.format ?? "",
    session.status ?? "",
    session.language ?? "",
    session.length !== undefined ? String(session.length) : "",
    ...(session.speakers?.map((s) => s.name ?? "") ?? []),
    ...(session.speakers?.map((s) => s.city ?? "") ?? []),
    ...(session.speakers?.map((s) => s.county ?? "") ?? []),
    ...(session.speakers?.map((s) => s.location ?? "") ?? []),
  ]
    .join(" ")
    .toLowerCase()

  return text.includes(needle)
}

const filteredSessions = computed(() => {
  const all = props.sessions ?? []
  const cityQ = (filters.city ?? "").trim().toUpperCase()
  const countyQ = (filters.county ?? "").trim().toUpperCase()

  return all
    .filter((s) => !filters.format || s.format === filters.format)
    .filter((s) => !filters.status || s.status === filters.status)
    .filter((s) => !filters.language || s.language === filters.language)
    .filter((s) => {
      if (!cityQ) return true
      return (s.speakers ?? []).some((sp) =>
        (sp.city ?? "").toUpperCase().includes(cityQ),
      )
    })
    .filter((s) => {
      if (!countyQ) return true
      return (s.speakers ?? []).some((sp) =>
        (sp.county ?? "").toUpperCase().includes(countyQ),
      )
    })
    .filter((s) => sessionMatchesSearch(s, search.value))
})

const total = computed(() => filteredSessions.value.length)
const totalPages = computed(() =>
  Math.max(1, Math.ceil(total.value / pageSize.value)),
)

watch(
  [
    search,
    () => filters.format,
    () => filters.status,
    () => filters.language,
    () => filters.city,
    () => filters.county,
    pageSize,
  ],
  () => {
    page.value = 1
  },
)

watch([total, pageSize], () => {
  if (page.value > totalPages.value) page.value = totalPages.value
  if (total.value === 0) page.value = 1
})

const paginatedSessions = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredSessions.value.slice(start, start + pageSize.value)
})

const rangeText = computed(() => {
  if (total.value === 0) return "Showing 0–0 of 0"
  const start = (page.value - 1) * pageSize.value + 1
  const end = Math.min(page.value * pageSize.value, total.value)
  return `Showing ${start}–${end} of ${total.value}`
})
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center gap-3">
      <UInput
        v-model="search"
        icon="i-lucide-search"
        placeholder="Search sessions (use filters below to narrow down by type, language or location)"
        class="w-full"
      />

      <USelect
        v-model="pageSize"
        :items="
          [25, 50, 100, 200].map((n) => ({ label: `${n}/page`, value: n }))
        "
        class="w-36"
      />
    </div>

    <SessionsFilters v-model="filters" />

    <div class="flex items-center justify-between gap-3">
      <div class="text-sm text-muted">{{ rangeText }}</div>
      <UPagination
        v-model:page="page"
        :total="total"
        :items-per-page="pageSize"
      />
    </div>

    <SessionsTable :sessions="paginatedSessions" :conference="conference" />

    <div class="flex justify-end">
      <UPagination
        v-model:page="page"
        :total="total"
        :items-per-page="pageSize"
      />
    </div>
  </div>
</template>

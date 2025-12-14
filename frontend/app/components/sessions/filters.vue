<script setup lang="ts">
type Filters = {
  format?: string
  status?: string
  language?: string
  city?: string
  county?: string
}

const model = defineModel<Filters>({ required: true })

const open = ref(false)

const formatItems = [
  { label: "Any", value: undefined },
  { label: "Presentation", value: "PRESENTATION" },
  { label: "Lightning Talk", value: "LIGHTNING_TALK" },
  { label: "Workshop", value: "WORKSHOP" },
  { label: "Panel Debate", value: "PANEL" },
]

const statusItems = [
  { label: "Any", value: undefined },
  { label: "Approved", value: "APPROVED" },
  { label: "Submitted", value: "SUBMITTED" },
  { label: "Rejected", value: "REJECTED" },
  { label: "Historic", value: "HISTORIC" },
  { label: "Draft", value: "DRAFT" },
]

const languageItems = [
  { label: "Any", value: undefined },
  { label: "English", value: "ENGLISH" },
  { label: "Norwegian", value: "NORWEGIAN" },
]

const hasAnyFilters = computed(() =>
  Boolean(
    model.value.format ||
    model.value.status ||
    model.value.language ||
    (model.value.city ?? "").trim() ||
    (model.value.county ?? "").trim(),
  ),
)

function clearAll() {
  model.value = {
    format: undefined,
    status: undefined,
    language: undefined,
    city: undefined,
    county: undefined,
  }
}
</script>

<template>
  <UCollapsible v-model:open="open" class="rounded-lg border border-default/60">
    <!-- trigger -->
    <UButton
      variant="ghost"
      color="neutral"
      class="w-full justify-between"
      trailing-icon="i-lucide-chevron-down"
    >
      <div class="flex items-center gap-3 min-w-0">
        <span class="font-semibold whitespace-nowrap">Filters</span>

        <div class="flex flex-wrap items-center gap-2 min-w-0">
          <UBadge v-if="model.format" variant="subtle">
            Format: {{ model.format }}
            <UButton
              size="xs"
              variant="link"
              icon="i-lucide-x"
              @click.stop="model.format = undefined"
            />
          </UBadge>

          <UBadge v-if="model.status" variant="subtle">
            Status: {{ model.status }}
            <UButton
              size="xs"
              variant="link"
              icon="i-lucide-x"
              @click.stop="model.status = undefined"
            />
          </UBadge>

          <UBadge v-if="model.language" variant="subtle">
            Language: {{ model.language }}
            <UButton
              size="xs"
              variant="link"
              icon="i-lucide-x"
              @click.stop="model.language = undefined"
            />
          </UBadge>

          <UBadge v-if="(model.city ?? '').trim()" variant="subtle">
            City: {{ model.city }}
            <UButton
              size="xs"
              variant="link"
              icon="i-lucide-x"
              @click.stop="model.city = undefined"
            />
          </UBadge>

          <UBadge v-if="(model.county ?? '').trim()" variant="subtle">
            County: {{ model.county }}
            <UButton
              size="xs"
              variant="link"
              icon="i-lucide-x"
              @click.stop="model.county = undefined"
            />
          </UBadge>
        </div>
      </div>

      <template #trailing>
        <div class="flex items-center gap-2">
          <UButton
            v-if="hasAnyFilters"
            size="xs"
            variant="ghost"
            color="neutral"
            icon="i-lucide-eraser"
            @click.stop="clearAll"
          >
            Clear
          </UButton>
          <UIcon
            name="i-lucide-chevron-down"
            class="transition-transform duration-200"
          />
        </div>
      </template>
    </UButton>

    <template #content>
      <div class="p-3 border-t border-default/60">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
          <UCard>
            <template #header>Format</template>
            <USelectMenu
              v-model="model.format"
              :items="formatItems"
              value-key="value"
              label-key="label"
              placeholder="Any"
              class="w-full"
              :ui="{ content: 'min-w-72' }"
            />
          </UCard>

          <UCard>
            <template #header>Status</template>
            <USelectMenu
              v-model="model.status"
              :items="statusItems"
              value-key="value"
              label-key="label"
              placeholder="Any"
              class="w-full"
              :ui="{ content: 'min-w-72' }"
            />
          </UCard>

          <UCard>
            <template #header>Language</template>
            <USelectMenu
              v-model="model.language"
              :items="languageItems"
              value-key="value"
              label-key="label"
              placeholder="Any"
              class="w-full"
              :ui="{ content: 'min-w-72' }"
            />
          </UCard>

          <UCard>
            <template #header>Location</template>
            <div class="flex flex-col gap-3">
              <UInput v-model="model.city" placeholder="City" />
              <UInput v-model="model.county" placeholder="County" />
            </div>
          </UCard>
        </div>
      </div>
    </template>
  </UCollapsible>
</template>

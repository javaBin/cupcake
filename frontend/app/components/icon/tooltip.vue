<script setup lang="ts">
  import { Icon } from '@iconify/vue'

  const tooltip = defineProps<{
    value: string
    select: string
    description: string
    name: string
    full?: boolean
  }>()
</script>

<template>
  <div v-if="tooltip.value === tooltip.select" class="d-inline">
    <v-tooltip :text="tooltip.description">
      <template #activator="{ props }">
        <Icon v-bind="props" :icon="tooltip.name" />
      </template>
    </v-tooltip>
    <span v-if="tooltip.full" class="mx-2">{{ tooltip.description }}</span>
  </div>
</template>

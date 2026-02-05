<script setup lang="ts">
const tooltip = defineProps<{
  value: string
  select: string
  description: string
  name: string
  full?: boolean
}>()
</script>

<template>
  <span
    v-if="tooltip.value === tooltip.select"
    class="inline-flex items-center gap-1.5"
  >
    <UTooltip :text="tooltip.description">
      <Icon :name="tooltip.name" class="cursor-help shrink-0" />
    </UTooltip>

    <span v-if="tooltip.full" class="text-sm">
      {{ tooltip.description }}
    </span>
  </span>
</template>

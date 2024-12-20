<script setup lang="ts">
import {Icon} from "@iconify/vue";

const tooltip = defineProps<{
  value: string,
  select: string,
  description: string,
  icon: string,
  full?: boolean
}>()
</script>

<template>
  <div class="d-inline" v-if="tooltip.value === tooltip.select">
    <v-tooltip :text="tooltip.description">
      <template v-slot:activator="{ props }">
        <Icon v-bind="props" :icon="tooltip.icon"/>
      </template>
    </v-tooltip>
    <span v-if="tooltip.full" class="mx-2">{{ tooltip.description }}</span>
  </div>
</template>
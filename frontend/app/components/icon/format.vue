<script setup lang="ts">
import {Icon} from "@iconify/vue";

const props = defineProps<{
  format: string,
  full?: boolean
}>()
</script>

<template>
  <span class="px-2">
    <IconTooltip :value="props.format" select="LIGHTNING_TALK" description="Lightning Talk" icon="carbon:lightning" :full="props.full" />
    <IconTooltip :value="props.format" select="WORKSHOP" description="Workshop" icon="carbon:laptop" :full="props.full" />
    <IconTooltip :value="props.format" select="PRESENTATION" description="Presentation" icon="carbon:group-presentation" :full="props.full" />
    <IconTooltip :value="props.format" select="PANEL" description="Panel Debate" icon="carbon:forum" :full="props.full" />
  </span>
</template>
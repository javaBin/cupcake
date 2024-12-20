<script setup lang="ts">
import {Icon} from "@iconify/vue";

const props = defineProps<{
  language: string,
  full?: boolean
}>()
</script>

<template>
  <span class="px-2">
    <IconTooltip :value="props.language" select="ENGLISH" description="English" icon="openmoji:flag-united-kingdom" :full="props.full" />
    <IconTooltip :value="props.language" select="NORWEGIAN" description="Norwegian" icon="openmoji:flag-norway" :full="props.full" />
  </span>
</template>
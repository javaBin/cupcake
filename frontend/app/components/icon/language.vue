<script setup lang="ts">
  const props = defineProps<{
    language: string
    full?: boolean
  }>()
</script>

<template>
  <span class="px-2">
    <IconTooltip
      :value="props.language"
      select="ENGLISH"
      description="English"
      name="openmoji:flag-united-kingdom"
      :full="props.full"
    />
    <IconTooltip
      :value="props.language"
      select="NORWEGIAN"
      description="Norwegian"
      name="openmoji:flag-norway"
      :full="props.full"
    />
  </span>
</template>

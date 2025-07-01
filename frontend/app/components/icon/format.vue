<script setup lang="ts">
  const props = defineProps<{
    format: string
    full?: boolean
  }>()
</script>

<template>
  <span class="px-2">
    <IconTooltip
      :value="props.format"
      select="LIGHTNING_TALK"
      description="Lightning Talk"
      name="carbon:lightning"
      :full="props.full"
    />
    <IconTooltip
      :value="props.format"
      select="WORKSHOP"
      description="Workshop"
      name="carbon:laptop"
      :full="props.full"
    />
    <IconTooltip
      :value="props.format"
      select="PRESENTATION"
      description="Presentation"
      name="carbon:group-presentation"
      :full="props.full"
    />
    <IconTooltip
      :value="props.format"
      select="PANEL"
      description="Panel Debate"
      name="carbon:forum"
      :full="props.full"
    />
  </span>
</template>

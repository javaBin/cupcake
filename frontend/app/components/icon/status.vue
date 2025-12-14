<script setup lang="ts">
const props = defineProps<{
  status: string
  full?: boolean
}>()
</script>

<template>
  <span class="px-2">
    <IconTooltip
      :value="props.status"
      select="APPROVED"
      description="Approved"
      name="carbon:task-approved"
      :full="props.full"
    />
    <IconTooltip
      :value="props.status"
      select="SUBMITTED"
      description="Submitted"
      name="carbon:task-view"
      :full="props.full"
    />
    <IconTooltip
      :value="props.status"
      select="REJECTED"
      description="Rejected"
      name="carbon:task-remove"
      :full="props.full"
    />
    <IconTooltip
      :value="props.status"
      select="HISTORIC"
      description="Historic"
      name="carbon:task-asset-view"
      :full="props.full"
    />
    <IconTooltip
      :value="props.status"
      select="DRAFT"
      description="Draft"
      name="carbon:task-add"
      :full="props.full"
    />
  </span>
</template>

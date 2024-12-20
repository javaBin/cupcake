<script setup lang="ts">
import {Icon} from "@iconify/vue";

const props = defineProps<{
  status: string,
  full?: boolean
}>()
</script>

<template>
  <span class="px-2">
    <IconTooltip :value="props.status" select="APPROVED" description="Approved" icon="carbon:task-approved" :full="props.full" />
    <IconTooltip :value="props.status" select="SUBMITTED" description="Submitted" icon="carbon:task-view" :full="props.full" />
    <IconTooltip :value="props.status" select="REJECTED" description="Rejected" icon="carbon:task-remove" :full="props.full" />
    <IconTooltip :value="props.status" select="HISTORIC" description="Historic" icon="carbon:task-asset-view" :full="props.full" />
    <IconTooltip :value="props.status" select="DRAFT" description="Draft" icon="carbon:task-add" :full="props.full" />
  </span>
</template>

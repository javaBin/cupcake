<script setup lang="ts">
import type { Session } from "@/types/session"
import type { Conference } from "@/types/conference"
import { h, resolveComponent } from "vue"
import type { TableColumn } from "@nuxt/ui"
import type { Component } from "vue"

const props = defineProps<{
  sessions: Session[]
  conference?: Conference
}>()

const { sessionLink } = useSessions()

const UButton = resolveComponent("UButton") as Component
const IconFormat = resolveComponent("IconFormat") as Component
const IconStatus = resolveComponent("IconStatus") as Component
const IconLanguage = resolveComponent("IconLanguage") as Component

const columns: TableColumn<Session>[] = [
  {
    accessorKey: "speakers",
    header: "Speaker",
    cell: ({ row }) => {
      const speakers = row.original.speakers ?? []
      return h(
        "div",
        { class: "space-y-2 max-w-[18rem]" },
        speakers.map((s, i) =>
          h("div", { key: i, class: "leading-tight" }, [
            h("div", { class: "font-medium break-words" }, s.name),
            h("div", { class: "text-sm text-muted break-all" }, s.email),
          ]),
        ),
      )
    },
  },
  {
    accessorKey: "title",
    header: "Title",
    meta: { class: { td: "w-full" } },
    cell: ({ row }) => {
      const title = h("span", { class: "break-words" }, row.original.title)
      if (row.original.id && props.conference) {
        return h("div", { class: "flex items-center gap-2 min-w-[28rem]" }, [
          h(UButton, {
            to: sessionLink(props.conference, row.original.id),
            variant: "ghost",
            color: "neutral",
            size: "xs",
            square: true,
            icon: "i-lucide-file-text",
            "aria-label": "Open session",
          }),
          title,
        ])
      }
      return h("div", { class: "min-w-[28rem]" }, [title])
    },
  },
  {
    accessorKey: "format",
    header: "Format",
    enableSorting: false,
    meta: {
      class: {
        th: "text-left whitespace-nowrap",
        td: "text-left whitespace-nowrap w-0",
      },
    },
    cell: ({ row }) =>
      h("div", { class: "inline-flex items-center gap-2 whitespace-nowrap" }, [
        h(IconFormat, { format: row.original.format, full: true }),
      ]),
  },
  {
    accessorKey: "status",
    header: "Status",
    enableSorting: false,
    meta: {
      class: {
        th: "text-left whitespace-nowrap",
        td: "text-left whitespace-nowrap w-0",
      },
    },
    cell: ({ row }) =>
      h("div", { class: "inline-flex items-center gap-2 whitespace-nowrap" }, [
        h(IconStatus, { status: row.original.status, full: true }),
      ]),
  },
  {
    accessorKey: "language",
    header: "Language",
    enableSorting: false,
    meta: {
      class: {
        th: "text-left whitespace-nowrap",
        td: "text-left whitespace-nowrap w-0",
      },
    },
    cell: ({ row }) =>
      h("div", { class: "inline-flex items-center gap-2 whitespace-nowrap" }, [
        h(IconLanguage, { language: row.original.language, full: true }),
      ]),
  },
  {
    accessorKey: "length",
    header: "Length",
    meta: {
      class: {
        th: "text-right whitespace-nowrap",
        td: "text-right whitespace-nowrap w-0",
      },
    },
    cell: ({ row }) =>
      h(
        "div",
        { class: "tabular-nums whitespace-nowrap" },
        row.original.length ?? "",
      ),
  },
  {
    id: "location",
    header: "Location",
    accessorFn: (r) => r.speakers,
    cell: ({ row }) => {
      const speakers = row.original.speakers ?? []
      return h(
        "div",
        { class: "space-y-2 max-w-[18rem]" },
        speakers.map((s, i) => {
          const parts = [s.location, s.city, s.county].filter(
            Boolean,
          ) as string[]
          return h("div", { key: i, class: "leading-tight" }, [
            h("div", { class: "font-medium break-words" }, s.postcode ?? ""),
            h(
              "div",
              { class: "text-sm text-muted break-words" },
              parts.join(" "),
            ),
          ])
        }),
      )
    },
  },
]
</script>

<template>
  <UTable
    :data="sessions"
    :columns="columns"
    :ui="{
      root: 'w-full overflow-x-auto',
      base: 'table-auto w-full',
      th: 'whitespace-nowrap',
      td: 'align-top',
    }"
  />
</template>

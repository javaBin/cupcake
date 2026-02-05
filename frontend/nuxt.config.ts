let frontend_host = "localhost"

if (
  "CUPCAKE_FRONTEND" in process.env &&
  process.env.CUPCAKE_FRONTEND !== undefined
) {
  frontend_host = process.env.CUPCAKE_FRONTEND
}

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2025-08-06",

  devtools: { enabled: true },
  modules: [
    "@nuxt/ui",
    "@nuxt/eslint",
    "@nuxt/icon",
    "@artmizu/nuxt-prometheus",
  ],
  vite: {
    server: {
      allowedHosts: ["maximum-whale-singularly.ngrok-free.app", frontend_host],
    },
  },
  icon: {
    serverBundle: {
      collections: ["carbon", "lucide", "logos", "openmoji"],
    },
  },

  css: ["~/assets/css/main.css"],

  colorMode: {
    preference: "dark",
    fallback: "dark",
    classSuffix: "",
  },
})

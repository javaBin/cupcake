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

  runtimeConfig: {
    public: {
      oidcAuthority: "https://auth.home.chrissearle.org/realms/HA12",
      oidcClientId: "cupcake-client",
    },
  },

  ssr: false,
  devtools: { enabled: true },
  modules: [
    "@nuxt/ui",
    "@nuxt/eslint",
    "@nuxt/icon",
    "@artmizu/nuxt-prometheus",
  ],
  vite: {
    server: {
      allowedHosts: [frontend_host],
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

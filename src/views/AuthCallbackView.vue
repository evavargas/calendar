<template>
  <main
    id="main"
    class="landing-section"
  >
    <UiAlert v-if="error">
      {{ error }}
    </UiAlert>
    <UiAlert
      v-else
      tone="info"
    >
      {{ t("auth.completing") }}
    </UiAlert>
  </main>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { UiAlert } from "../components/ui";
import { isMockApi } from "../services/api/client";
import { useAuthStore } from "../stores/auth";

const { t } = useI18n();
const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const error = ref("");

onMounted(async () => {
  try {
    if (isMockApi() || route.query.mock === "1") {
      await auth.completeMockLogin();
    } else {
      await auth.bootstrap({ force: true });
      if (!auth.isAuthenticated) {
        throw new Error("No se recibió sesión del backend.");
      }
    }
    const next = typeof route.query.next === "string" ? route.query.next : "/app";
    router.replace(next.startsWith("/app") ? next : { name: "today" });
  } catch (cause) {
    error.value =
      cause instanceof Error ? cause.message : "Falló el inicio de sesión.";
  }
});
</script>

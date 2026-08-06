<template>
  <div class="app-shell">
    <p
      v-if="showMockBanner"
      class="dev-banner"
      role="status"
    >
      Modo demo sin credenciales: auth y Google Calendar están mockeados.
      Los datos viven en {{ mockTarget }}.
    </p>
    <header class="site-nav">
      <RouterLink
        class="brand-mark"
        :to="{ name: 'today' }"
      >
        Kai<span>ro</span>
      </RouterLink>
      <div class="page-actions">
        <UiBadge v-if="auth.user">
          {{ auth.user.name }}
        </UiBadge>
        <UiButton
          variant="ghost"
          type="button"
          @click="onLogout"
        >
          Salir
        </UiButton>
      </div>
    </header>
    <div class="app-frame">
      <aside class="app-sidebar">
        <nav aria-label="Principal">
          <RouterLink :to="{ name: 'today' }">
            Hoy
          </RouterLink>
          <RouterLink :to="{ name: 'planner' }">
            Planes
          </RouterLink>
          <RouterLink :to="{ name: 'plan-new' }">
            Nuevo plan
          </RouterLink>
          <RouterLink :to="{ name: 'types' }">
            Tipos
          </RouterLink>
          <RouterLink :to="{ name: 'calendar' }">
            Calendario
          </RouterLink>
          <RouterLink :to="{ name: 'settings' }">
            Ajustes
          </RouterLink>
        </nav>
      </aside>
      <main
        id="main"
        class="app-main"
      >
        <RouterView />
      </main>
    </div>
    <UiFab
      v-if="showFab"
      :to="{ name: 'plan-new' }"
      aria-label="Nuevo plan"
    />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { UiBadge, UiButton, UiFab } from "../ui";
import { isMockApi } from "../../services/api/client";
import { useAuthStore } from "../../stores/auth";

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();

const showMockBanner = computed(() => {
  if (isMockApi()) return true;
  return Boolean(auth.user?.demoMode);
});
const mockTarget = computed(() =>
  isMockApi() ? "localStorage del browser" : "API local (H2 + KAIRO_AUTH_MOCK)"
);
const showFab = computed(
  () => route.name !== "plan-new" && route.name !== "plan-edit"
);

const onLogout = async () => {
  await auth.logout();
  router.push({ name: "landing" });
};
</script>

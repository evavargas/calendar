<template>
  <div class="app-shell">
    <p
      v-if="showMockBanner"
      class="dev-banner"
      role="status"
    >
      {{ t("shell.mockBanner", { target: mockTarget }) }}
    </p>
    <header class="site-nav">
      <RouterLink
        class="brand-mark"
        :to="{ name: 'today' }"
      >
        Kai<span>ro</span>
      </RouterLink>
      <div class="page-actions">
        <NavPrefs />
        <UiBadge v-if="auth.user">
          {{ auth.user.name }}
        </UiBadge>
        <UiButton
          variant="ghost"
          type="button"
          :disabled="loggingOut"
          @click="onLogout"
        >
          {{ t("nav.logout") }}
        </UiButton>
      </div>
    </header>
    <div class="app-frame">
      <aside class="app-sidebar">
        <nav :aria-label="t('nav.primary')">
          <RouterLink :to="{ name: 'today' }">
            {{ t("nav.today") }}
          </RouterLink>
          <RouterLink :to="{ name: 'planner' }">
            {{ t("nav.plans") }}
          </RouterLink>
          <RouterLink :to="{ name: 'plan-new' }">
            {{ t("nav.newPlan") }}
          </RouterLink>
          <RouterLink :to="{ name: 'types' }">
            {{ t("nav.types") }}
          </RouterLink>
          <RouterLink :to="{ name: 'calendar' }">
            {{ t("nav.calendar") }}
          </RouterLink>
          <RouterLink :to="{ name: 'settings' }">
            {{ t("nav.settings") }}
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
      :aria-label="t('nav.newPlan')"
    />
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { UiBadge, UiButton, UiFab } from "../ui";
import NavPrefs from "./NavPrefs.vue";
import { isMockApi } from "../../services/api/client";
import { useAuthStore } from "../../stores/auth";

const { t } = useI18n();
const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const loggingOut = ref(false);

const showMockBanner = computed(() => {
  if (isMockApi()) return true;
  return Boolean(auth.user?.demoMode);
});
const mockTarget = computed(() =>
  isMockApi() ? t("shell.mockBrowser") : t("shell.mockApi")
);
const showFab = computed(
  () => route.name !== "plan-new" && route.name !== "plan-edit"
);

const onLogout = async () => {
  if (loggingOut.value) return;
  loggingOut.value = true;
  try {
    await auth.logout();
  } finally {
    // replace: avoid landing→today bounce stacks; always leave /app
    await router.replace({ name: "landing" });
    loggingOut.value = false;
  }
};
</script>

<template>
  <div class="landing">
    <header class="site-nav">
      <RouterLink
        class="brand-mark"
        to="/"
      >
        Kai<span>ro</span>
      </RouterLink>
      <div class="page-actions">
        <NavPrefs />
        <UiButton
          variant="primary"
          :href="loginHref"
          @click="onLoginClick"
        >
          {{ t("landing.login") }}
        </UiButton>
      </div>
    </header>

    <UiAlert
      v-if="authError"
      class="landing-auth-error"
    >
      {{ t("landing.authError") }}
    </UiAlert>

    <section
      class="landing-hero"
      :aria-label="t('landing.presentation')"
    >
      <div class="landing-hero-inner">
        <p class="landing-brand">
          Kai<span>ro</span>
        </p>
        <h1>{{ t("landing.headline") }}</h1>
        <p class="support">
          {{ t("landing.support") }}
        </p>
        <div class="landing-cta">
          <UiButton
            variant="hero-primary"
            :href="loginHref"
            @click="onLoginClick"
          >
            {{ t("landing.start") }}
          </UiButton>
          <UiButton
            variant="hero-secondary"
            href="#como"
          >
            {{ t("landing.howItWorks") }}
          </UiButton>
        </div>
      </div>
    </section>

    <section
      id="como"
      class="landing-section"
    >
      <h2>{{ t("landing.sectionTitle") }}</h2>
      <p>
        {{ t("landing.sectionBody") }}
      </p>
      <div class="feature-grid">
        <UiSurface tag="article">
          <h3>{{ t("landing.featureTypesTitle") }}</h3>
          <p>
            {{ t("landing.featureTypesBody") }}
          </p>
        </UiSurface>
        <UiSurface tag="article">
          <h3>{{ t("landing.featureIcsTitle") }}</h3>
          <p>
            {{ t("landing.featureIcsBody") }}
          </p>
        </UiSurface>
        <UiSurface tag="article">
          <h3>{{ t("landing.featureGoogleTitle") }}</h3>
          <p>
            {{ t("landing.featureGoogleBody") }}
          </p>
        </UiSurface>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { UiAlert, UiButton, UiSurface } from "../components/ui";
import NavPrefs from "../components/layout/NavPrefs.vue";
import { authLoginUrl, isMockApi } from "../services/api/client";

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const loginHref = authLoginUrl();
const authError = computed(() => typeof route.query.authError === "string");

onMounted(() => {
  if (authError.value) {
    console.warn("Login falló:", route.query.authError);
  }
});

const onLoginClick = (event) => {
  if (!isMockApi()) return;
  event.preventDefault();
  router.push({ name: "auth-callback", query: { mock: "1" } });
};
</script>

<template>
  <section>
    <header class="page-header">
      <div>
        <h1>{{ t("settings.title") }}</h1>
        <p>{{ t("settings.subtitle") }}</p>
      </div>
    </header>

    <UiAlert
      v-if="message"
      tone="info"
    >
      {{ message }}
    </UiAlert>
    <UiAlert v-if="error">
      {{ error }}
    </UiAlert>

    <UiSurface
      tag="article"
      class="detail-panel settings-panel"
    >
      <h2>{{ t("settings.session") }}</h2>
      <p class="lead">
        {{ auth.user?.name }} · {{ auth.user?.email }}
      </p>
      <p
        v-if="isMockApi()"
        class="lead"
      >
        {{ t("settings.mockMode") }}
      </p>
    </UiSurface>

    <UiSurface
      tag="article"
      class="detail-panel"
    >
      <h2>{{ t("settings.google") }}</h2>
      <p class="lead">
        {{
          google.connected
            ? t("settings.googleConnected")
            : t("settings.googleDisconnected")
        }}
      </p>
      <div class="page-actions">
        <UiButton
          v-if="!google.connected"
          variant="primary"
          :href="connectHref"
          @click="onConnectClick"
        >
          {{ t("settings.connectGoogle") }}
        </UiButton>
        <UiButton
          v-else
          variant="secondary"
          type="button"
          @click="onDisconnect"
        >
          {{ t("settings.disconnect") }}
        </UiButton>
      </div>
    </UiSurface>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { UiAlert, UiButton, UiSurface } from "../components/ui";
import { googleConnectUrl, isMockApi } from "../services/api/client";
import { useAuthStore } from "../stores/auth";
import { useGoogleStore } from "../stores/google";

const { t } = useI18n();
const auth = useAuthStore();
const google = useGoogleStore();
const route = useRoute();
const router = useRouter();
const message = ref("");
const error = ref("");
const connectHref = googleConnectUrl();

onMounted(async () => {
  await google.refresh();
  if (route.query.google === "connected") {
    if (isMockApi()) {
      await google.connectMock();
    } else {
      await google.refresh();
    }
    message.value = t("settings.googleConnectedMsg");
    router.replace({ name: "settings" });
  }
});

const onConnectClick = async (event) => {
  if (!isMockApi()) {
    return;
  }
  event.preventDefault();
  error.value = "";
  try {
    await google.connectMock();
    message.value = t("settings.googleConnectedMock");
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : t("settings.connectError");
  }
};

const onDisconnect = async () => {
  error.value = "";
  try {
    await google.disconnect();
    message.value = t("settings.googleDisconnectedMsg");
  } catch (cause) {
    error.value =
      cause instanceof Error ? cause.message : t("settings.disconnectError");
  }
};
</script>

<style scoped>
.settings-panel {
  margin-bottom: var(--space-4);
}

.settings-panel h2,
.detail-panel h2 {
  margin: 0;
}
</style>

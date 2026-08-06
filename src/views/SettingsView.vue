<template>
  <section>
    <header class="page-header">
      <div>
        <h1>Ajustes</h1>
        <p>Cuenta, apariencia y conexión con Google Calendar.</p>
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
      class="detail-panel"
      style="margin-bottom: 1rem"
    >
      <h2 style="margin: 0">
        Apariencia
      </h2>
      <p class="lead">
        Tema claro u oscuro. Se guarda en este navegador.
      </p>
      <div
        class="theme-toggle"
        role="group"
        aria-label="Tema"
      >
        <UiButton
          :variant="theme === THEME_LIGHT ? 'primary' : 'secondary'"
          type="button"
          @click="setTheme(THEME_LIGHT)"
        >
          Claro
        </UiButton>
        <UiButton
          :variant="theme === THEME_DARK ? 'primary' : 'secondary'"
          type="button"
          @click="setTheme(THEME_DARK)"
        >
          Oscuro
        </UiButton>
      </div>
    </UiSurface>

    <UiSurface
      tag="article"
      class="detail-panel"
      style="margin-bottom: 1rem"
    >
      <h2 style="margin: 0">
        Sesión
      </h2>
      <p class="lead">
        {{ auth.user?.name }} · {{ auth.user?.email }}
      </p>
      <p
        v-if="isMockApi()"
        class="lead"
      >
        Modo mock activo (`VITE_USE_MOCK_API`). El login simula Google sin backend.
      </p>
    </UiSurface>

    <UiSurface
      tag="article"
      class="detail-panel"
    >
      <h2 style="margin: 0">
        Google Calendar
      </h2>
      <p class="lead">
        {{
          google.connected
            ? "Conectado. Podés enviar planes desde el detalle."
            : "No conectado. El login de identidad no alcanza para escribir en Calendar."
        }}
      </p>
      <div class="page-actions">
        <UiButton
          v-if="!google.connected"
          variant="primary"
          :href="connectHref"
          @click="onConnectClick"
        >
          Conectar Google Calendar
        </UiButton>
        <UiButton
          v-else
          variant="secondary"
          type="button"
          @click="onDisconnect"
        >
          Desconectar
        </UiButton>
      </div>
    </UiSurface>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { UiAlert, UiButton, UiSurface } from "../components/ui";
import { THEME_DARK, THEME_LIGHT, useTheme } from "../composables/useTheme";
import { googleConnectUrl, isMockApi } from "../services/api/client";
import { useAuthStore } from "../stores/auth";
import { useGoogleStore } from "../stores/google";

const auth = useAuthStore();
const google = useGoogleStore();
const route = useRoute();
const router = useRouter();
const { theme, setTheme } = useTheme();
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
    message.value = "Google Calendar conectado.";
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
    message.value = "Google Calendar conectado (mock).";
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "No se pudo conectar.";
  }
};

const onDisconnect = async () => {
  error.value = "";
  try {
    await google.disconnect();
    message.value = "Google Calendar desconectado.";
  } catch (cause) {
    error.value =
      cause instanceof Error ? cause.message : "No se pudo desconectar.";
  }
};
</script>

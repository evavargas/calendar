<template>
  <div class="landing">
    <header class="site-nav">
      <RouterLink
        class="brand-mark"
        to="/"
      >
        Kai<span>ro</span>
      </RouterLink>
      <UiButton
        variant="primary"
        :href="loginHref"
        @click="onLoginClick"
      >
        Entrar con Google
      </UiButton>
    </header>

    <UiAlert
      v-if="authError"
      class="landing-auth-error"
    >
      No se pudo iniciar sesión con Google. Probá de nuevo.
    </UiAlert>

    <section
      class="landing-hero"
      aria-label="Presentación"
    >
      <div class="landing-hero-inner">
        <p class="landing-brand">
          Kai<span>ro</span>
        </p>
        <h1>Planificá con intención. El calendario es solo el mapa.</h1>
        <p class="support">
          Organizá planes por tipo, describí el para qué, y cuando haga falta
          exportá a .ics o empujá a Google Calendar.
        </p>
        <div class="landing-cta">
          <UiButton
            variant="hero-primary"
            :href="loginHref"
            @click="onLoginClick"
          >
            Empezar con Google
          </UiButton>
          <UiButton
            variant="hero-secondary"
            href="#como"
          >
            Cómo funciona
          </UiButton>
        </div>
      </div>
    </section>

    <section
      id="como"
      class="landing-section"
    >
      <h2>Una app de planning, no otra grilla de fechas</h2>
      <p>
        Kairo pone el foco en lo que vas a hacer: tipo, descripción y estado.
        La vista de calendario queda como proyección secundaria.
      </p>
      <div class="feature-grid">
        <UiSurface tag="article">
          <h3>Tipos con color</h3>
          <p>
            Trabajo, salud, estudio o los que definas. El color viene del tipo,
            no de un picker suelto.
          </p>
        </UiSurface>
        <UiSurface tag="article">
          <h3>Export ICS</h3>
          <p>
            Descargá un plan o todo el filtro actual y abrilo en cualquier
            cliente de calendario.
          </p>
        </UiSurface>
        <UiSurface tag="article">
          <h3>Google opcional</h3>
          <p>
            Login con Google. La sync de Calendar es una conexión aparte, cuando
            la necesites.
          </p>
        </UiSurface>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { UiAlert, UiButton, UiSurface } from "../components/ui";
import { authLoginUrl, isMockApi } from "../services/api/client";

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

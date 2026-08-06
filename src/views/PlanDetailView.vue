<template>
  <section v-if="plan">
    <header class="page-header">
      <div>
        <h1>{{ plan.title }}</h1>
        <p>{{ formatPlanWhen(plan) }}</p>
      </div>
      <div class="page-actions">
        <UiButton
          variant="secondary"
          type="button"
          @click="onExport"
        >
          Descargar ICS
        </UiButton>
        <UiButton
          variant="secondary"
          type="button"
          :disabled="google.loading"
          @click="onPushGoogle"
        >
          Enviar a Google
        </UiButton>
        <UiButton
          variant="primary"
          :to="{ name: 'plan-edit', params: { id: plan.id } }"
        >
          Editar
        </UiButton>
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
    >
      <div class="plan-meta">
        <UiBadge :dot="typeColor">
          {{ typeName }}
        </UiBadge>
        <UiBadge>{{ statusLabel(plan.status) }}</UiBadge>
      </div>
      <p class="lead">
        {{ plan.description || "Sin descripción." }}
      </p>
      <div class="page-actions">
        <UiButton
          v-if="plan.status !== 'done'"
          variant="secondary"
          type="button"
          @click="setStatus('done')"
        >
          Marcar hecho
        </UiButton>
        <UiButton
          v-if="plan.status !== 'planned'"
          variant="secondary"
          type="button"
          @click="setStatus('planned')"
        >
          Volver a planificado
        </UiButton>
        <UiButton
          variant="danger"
          type="button"
          @click="onRemove"
        >
          Eliminar
        </UiButton>
      </div>
    </UiSurface>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { UiAlert, UiBadge, UiButton, UiSurface } from "../components/ui";
import { formatPlanWhen, statusLabel } from "../composables/format";
import { useGoogleStore } from "../stores/google";
import { usePlansStore } from "../stores/plans";
import { useTypesStore } from "../stores/types";

const route = useRoute();
const router = useRouter();
const plans = usePlansStore();
const types = useTypesStore();
const google = useGoogleStore();
const message = ref("");
const error = ref("");

const plan = computed(() => plans.current);
const type = computed(() => (plan.value ? types.byId[plan.value.typeId] : null));
const typeName = computed(() => type.value?.name || "Sin tipo");
const typeColor = computed(() => type.value?.color || "#0f7a6c");

onMounted(async () => {
  await Promise.all([
    types.refresh(),
    google.refresh(),
    plans.loadOne(route.params.id),
  ]);
});

const setStatus = async (status) => {
  error.value = "";
  await plans.updatePlan(plan.value.id, { status });
};

const onExport = async () => {
  error.value = "";
  try {
    await plans.exportPlanIcs(plan.value.id);
    message.value = "ICS descargado.";
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "No se pudo exportar.";
  }
};

const onPushGoogle = async () => {
  error.value = "";
  message.value = "";
  try {
    if (!google.connected) {
      error.value = "Conectá Google Calendar en Ajustes primero.";
      return;
    }
    const result = await google.pushPlan(plan.value.id);
    message.value = `Enviado a Google (${result.googleEventId}).`;
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "Falló el envío.";
  }
};

const onRemove = async () => {
  if (!window.confirm("¿Eliminar este plan?")) return;
  await plans.removePlan(plan.value.id);
  router.push({ name: "today" });
};
</script>

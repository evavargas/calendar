<template>
  <section v-if="plan">
    <header class="page-header">
      <div>
        <h1>{{ plan.title }}</h1>
        <p>{{ whenLabel }}</p>
      </div>
      <div class="page-actions">
        <UiButton
          variant="secondary"
          type="button"
          @click="onExport"
        >
          {{ t("plan.downloadIcs") }}
        </UiButton>
        <UiButton
          variant="secondary"
          type="button"
          :disabled="google.loading"
          @click="onPushGoogle"
        >
          {{ t("plan.pushGoogle") }}
        </UiButton>
        <UiButton
          variant="primary"
          :to="{ name: 'plan-edit', params: { id: plan.id } }"
        >
          {{ t("plan.edit") }}
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
        <UiBadge :tone="plan.status">
          {{ t(statusKey(plan.status)) }}
        </UiBadge>
      </div>
      <p class="lead">
        {{ plan.description || "—" }}
      </p>
      <div class="page-actions">
        <UiButton
          v-if="plan.status !== 'done'"
          variant="secondary"
          type="button"
          @click="setStatus('done')"
        >
          {{ t("plan.markDone") }}
        </UiButton>
        <UiButton
          v-if="plan.status !== 'planned'"
          variant="secondary"
          type="button"
          @click="setStatus('planned')"
        >
          {{ t("plan.markPlanned") }}
        </UiButton>
        <UiButton
          variant="danger"
          type="button"
          @click="onRemove"
        >
          {{ t("plan.delete") }}
        </UiButton>
      </div>
    </UiSurface>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { UiAlert, UiBadge, UiButton, UiSurface } from "../components/ui";
import { formatPlanWhen, statusKey } from "../composables/format";
import { useLocale } from "../composables/useLocale";
import { useGoogleStore } from "../stores/google";
import { usePlansStore } from "../stores/plans";
import { useTypesStore } from "../stores/types";

const { t } = useI18n();
const { dateLocale } = useLocale();
const route = useRoute();
const router = useRouter();
const plans = usePlansStore();
const types = useTypesStore();
const google = useGoogleStore();
const message = ref("");
const error = ref("");

const plan = computed(() => plans.current);
const type = computed(() => (plan.value ? types.byId[plan.value.typeId] : null));
const typeName = computed(() => type.value?.name || t("plan.noType"));
const typeColor = computed(() => type.value?.color || "#0f7a6c");
const whenLabel = computed(() =>
  plan.value ? formatPlanWhen(plan.value, dateLocale.value) : ""
);

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
    message.value = t("plan.icsDownloaded");
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "No se pudo exportar.";
  }
};

const onPushGoogle = async () => {
  error.value = "";
  message.value = "";
  try {
    if (!google.connected) {
      error.value = t("plan.googleNeedConnect");
      return;
    }
    const result = await google.pushPlan(plan.value.id);
    message.value = `Google · ${result.googleEventId}`;
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "Falló el envío.";
  }
};

const onRemove = async () => {
  if (!window.confirm(t("plan.deleteConfirm"))) return;
  await plans.removePlan(plan.value.id);
  router.push({ name: "today" });
};
</script>

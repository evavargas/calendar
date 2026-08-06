<template>
  <section class="planner-page">
    <header class="page-header">
      <div>
        <h1>Tus planes</h1>
        <p>Lista completa. El día a día vive en Hoy.</p>
      </div>
      <div class="page-actions">
        <UiButton
          variant="secondary"
          type="button"
          :disabled="plans.loading"
          @click="onExport"
        >
          Descargar ICS
        </UiButton>
        <UiButton
          variant="primary"
          :to="{ name: 'plan-new' }"
        >
          Nuevo plan
        </UiButton>
      </div>
    </header>

    <div
      class="status-summary"
      role="group"
      aria-label="Resumen por estado"
    >
      <button
        type="button"
        class="status-pill"
        :data-active="plans.filters.status === 'planned' || undefined"
        @click="setStatusFilter('planned')"
      >
        <span class="status-pill-count">{{ plans.counts.planned }}</span>
        <span>Planificados</span>
      </button>
      <button
        type="button"
        class="status-pill"
        :data-active="plans.filters.status === 'done' || undefined"
        @click="setStatusFilter('done')"
      >
        <span class="status-pill-count">{{ plans.counts.done }}</span>
        <span>Hechos</span>
      </button>
      <button
        type="button"
        class="status-pill"
        :data-active="plans.filters.status === 'cancelled' || undefined"
        @click="setStatusFilter('cancelled')"
      >
        <span class="status-pill-count">{{ plans.counts.cancelled }}</span>
        <span>Cancelados</span>
      </button>
      <button
        type="button"
        class="status-pill"
        :data-active="!plans.filters.status || undefined"
        @click="setStatusFilter('')"
      >
        <span class="status-pill-count">{{ plans.counts.total }}</span>
        <span>Todos</span>
      </button>
    </div>

    <div class="filters">
      <label
        class="sr-only"
        for="filter-type"
      >Tipo</label>
      <select
        id="filter-type"
        v-model="plans.filters.typeId"
        @change="plans.refresh()"
      >
        <option value="">
          Todos los tipos
        </option>
        <option
          v-for="type in types.items"
          :key="type.id"
          :value="type.id"
        >
          {{ type.name }}
        </option>
      </select>
    </div>

    <UiAlert v-if="plans.error">
      {{ plans.error }}
    </UiAlert>

    <UiEmptyState
      v-if="!plans.loading && plans.items.length === 0"
      title="Todavía no hay planes"
    >
      <p>Creá el primero y asignale un tipo para que el color tenga sentido.</p>
      <template #actions>
        <UiButton
          variant="primary"
          :to="{ name: 'plan-new' }"
        >
          Crear plan
        </UiButton>
      </template>
    </UiEmptyState>

    <div
      v-else
      class="plan-list"
    >
      <RouterLink
        v-for="plan in plans.items"
        :key="plan.id"
        :to="{ name: 'plan-detail', params: { id: plan.id } }"
      >
        <PlanCard :plan="plan" />
      </RouterLink>
    </div>
  </section>
</template>

<script setup>
import { onMounted } from "vue";
import { RouterLink } from "vue-router";
import PlanCard from "../components/plans/PlanCard.vue";
import { UiAlert, UiButton, UiEmptyState } from "../components/ui";
import { usePlansStore } from "../stores/plans";
import { useTypesStore } from "../stores/types";

const plans = usePlansStore();
const types = useTypesStore();

onMounted(async () => {
  await Promise.all([types.refresh(), plans.refresh()]);
});

const setStatusFilter = async (status) => {
  plans.filters.status = status;
  await plans.refresh();
};

const onExport = async () => {
  try {
    await plans.exportIcs();
  } catch (cause) {
    plans.error =
      cause instanceof Error ? cause.message : "No se pudo exportar ICS.";
  }
};
</script>

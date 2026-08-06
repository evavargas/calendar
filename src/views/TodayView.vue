<template>
  <section class="today-view">
    <header class="page-header">
      <div>
        <p class="today-kicker">
          {{ greeting }}
        </p>
        <h1>{{ isSelectedToday ? "Hoy" : selectedLabel }}</h1>
        <p>{{ monthLabel }}</p>
      </div>
      <div class="page-actions">
        <UiButton
          variant="primary"
          :to="{ name: 'plan-new', query: { date: selectedKey } }"
        >
          Nuevo plan
        </UiButton>
      </div>
    </header>

    <div
      class="day-strip"
      role="listbox"
      aria-label="Días de la semana"
    >
      <button
        v-for="day in weekDays"
        :key="day.key"
        type="button"
        class="day-chip"
        role="option"
        :aria-selected="day.key === selectedKey"
        :data-today="day.isToday || undefined"
        :data-active="day.key === selectedKey || undefined"
        @click="selected = day.date"
      >
        <span class="day-chip-dow">{{ day.dow }}</span>
        <span class="day-chip-num">{{ day.dayNum }}</span>
      </button>
    </div>

    <UiAlert v-if="error">
      {{ error }}
    </UiAlert>

    <UiEmptyState v-if="loading">
      <p>Cargando el día…</p>
    </UiEmptyState>

    <template v-else>
      <div
        v-if="allDayPlans.length"
        class="all-day-row"
      >
        <span class="all-day-label">Todo el día</span>
        <div class="all-day-blocks">
          <RouterLink
            v-for="plan in allDayPlans"
            :key="plan.id"
            class="timeline-block is-all-day"
            :style="blockStyle(plan)"
            :to="{ name: 'plan-detail', params: { id: plan.id } }"
          >
            <strong>{{ plan.title }}</strong>
          </RouterLink>
        </div>
      </div>

      <UiEmptyState
        v-if="!timedPlans.length && !allDayPlans.length"
        title="Nada en este día"
      >
        <p>Agregá un plan para verlo como bloque en la línea de tiempo.</p>
        <template #actions>
          <UiButton
            variant="primary"
            :to="{ name: 'plan-new', query: { date: selectedKey } }"
          >
            Crear plan
          </UiButton>
        </template>
      </UiEmptyState>

      <div
        v-else-if="timedPlans.length"
        class="day-timeline"
        :style="{ '--hours': String(hours.length) }"
      >
        <div
          class="timeline-hours"
          aria-hidden="true"
        >
          <div
            v-for="hour in hours"
            :key="hour"
            class="timeline-hour"
          >
            {{ formatHourLabel(hour) }}
          </div>
        </div>
        <div class="timeline-track">
          <div
            v-for="hour in hours"
            :key="`line-${hour}`"
            class="timeline-gridline"
          />
          <article
            v-for="plan in timedPlans"
            :key="plan.id"
            class="timeline-block"
            :style="blockPosition(plan)"
          >
            <RouterLink :to="{ name: 'plan-detail', params: { id: plan.id } }">
              <strong>{{ plan.title }}</strong>
              <span class="timeline-when">{{ formatPlanWhen(plan) }}</span>
              <p
                v-if="plan.description"
                class="timeline-desc"
                :data-expanded="expandedId === plan.id || undefined"
              >
                {{ plan.description }}
              </p>
            </RouterLink>
            <button
              v-if="plan.description && plan.description.length > 90"
              class="timeline-more"
              type="button"
              @click.stop="toggleExpanded(plan.id)"
            >
              {{ expandedId === plan.id ? "Ver menos" : "Leer más" }}
            </button>
          </article>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { RouterLink } from "vue-router";
import { UiAlert, UiButton, UiEmptyState } from "../components/ui";
import {
  addDays,
  endOfDay,
  formatDayStripLabel,
  formatHourLabel,
  formatMonthLabel,
  formatPlanWhen,
  planOverlapsDay,
  startOfDay,
  toDateKey,
} from "../composables/format";
import { useAuthStore } from "../stores/auth";
import { usePlansStore } from "../stores/plans";
import { useTypesStore } from "../stores/types";

const DAY_START_HOUR = 7;
const DAY_END_HOUR = 21;

const auth = useAuthStore();
const plans = usePlansStore();
const types = useTypesStore();

const selected = ref(startOfDay(new Date()));
const dayPlans = ref([]);
const loading = ref(false);
const error = ref("");
const expandedId = ref("");

const selectedKey = computed(() => toDateKey(selected.value));
const isSelectedToday = computed(() => selectedKey.value === toDateKey(new Date()));
const monthLabel = computed(() => formatMonthLabel(selected.value));
const selectedLabel = computed(() =>
  selected.value.toLocaleDateString("es-AR", {
    weekday: "long",
    day: "numeric",
    month: "short",
  })
);

const greeting = computed(() => {
  const name = auth.user?.name?.split(" ")[0];
  const hour = new Date().getHours();
  const hello = hour < 12 ? "Buenos días" : hour < 19 ? "Buenas tardes" : "Buenas noches";
  return name ? `${hello}, ${name}` : hello;
});

const weekDays = computed(() => {
  const today = startOfDay(new Date());
  // Show selected week's Sunday–Saturday, or centered week around selected
  const dow = selected.value.getDay();
  const sunday = addDays(selected.value, -dow);
  return Array.from({ length: 7 }, (_, index) => {
    const date = addDays(sunday, index);
    return {
      date,
      key: toDateKey(date),
      dow: formatDayStripLabel(date),
      dayNum: date.getDate(),
      isToday: toDateKey(date) === toDateKey(today),
    };
  });
});

const allDayPlans = computed(() => dayPlans.value.filter((plan) => plan.allDay));
const timedPlans = computed(() => dayPlans.value.filter((plan) => !plan.allDay));

const hours = computed(() => {
  let min = DAY_START_HOUR;
  let max = DAY_END_HOUR;
  timedPlans.value.forEach((plan) => {
    const start = new Date(plan.startsAt);
    const end = new Date(plan.endsAt);
    min = Math.min(min, start.getHours());
    max = Math.max(max, end.getHours() + (end.getMinutes() > 0 ? 1 : 0));
  });
  min = Math.max(0, Math.min(min, DAY_START_HOUR));
  max = Math.min(23, Math.max(max, DAY_END_HOUR));
  return Array.from({ length: max - min + 1 }, (_, i) => min + i);
});

const typeColor = (plan) => types.byId[plan.typeId]?.color || "#0f7a6c";

const blockStyle = (plan) => ({
  "--block-color": typeColor(plan),
  background: `color-mix(in srgb, ${typeColor(plan)} 22%, white)`,
  borderColor: typeColor(plan),
});

const blockPosition = (plan) => {
  const firstHour = hours.value[0] ?? DAY_START_HOUR;
  const dayBase = startOfDay(selected.value);
  dayBase.setHours(firstHour, 0, 0, 0);
  const start = new Date(plan.startsAt);
  const end = new Date(plan.endsAt);
  const minutesFromBase = Math.max(0, (start - dayBase) / 60000);
  const duration = Math.max(30, (end - start) / 60000);
  const pxPerMinute = 64 / 60;
  return {
    ...blockStyle(plan),
    top: `${minutesFromBase * pxPerMinute}px`,
    height: `${duration * pxPerMinute}px`,
  };
};

const toggleExpanded = (id) => {
  expandedId.value = expandedId.value === id ? "" : id;
};

const loadDay = async () => {
  loading.value = true;
  error.value = "";
  try {
    const from = startOfDay(selected.value).toISOString();
    const to = endOfDay(selected.value).toISOString();
    const listed = await plans.listRange({ from, to });
    dayPlans.value = listed
      .filter((plan) => planOverlapsDay(plan, selected.value))
      .filter((plan) => plan.status !== "cancelled")
      .sort((a, b) => new Date(a.startsAt) - new Date(b.startsAt));
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "No se pudo cargar el día.";
    dayPlans.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  await types.refresh();
  await loadDay();
});

watch(selected, () => {
  expandedId.value = "";
  loadDay();
});
</script>

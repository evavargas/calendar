<template>
  <section>
    <header class="page-header">
      <div>
        <h1>{{ t("calendar.title") }}</h1>
        <p>{{ t("calendar.subtitle") }}</p>
      </div>
      <UiButton
        variant="primary"
        :to="{ name: 'today' }"
      >
        {{ t("nav.today") }}
      </UiButton>
    </header>

    <div class="calendar-wrap">
      <FullCalendar :options="calendarOptions" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import esLocale from "@fullcalendar/core/locales/es";
import enLocale from "@fullcalendar/core/locales/en-gb";
import { UiButton } from "../components/ui";
import { useLocale } from "../composables/useLocale";
import { usePlansStore } from "../stores/plans";
import { useTypesStore } from "../stores/types";

const { t } = useI18n();
const { locale } = useLocale();
const plans = usePlansStore();
const types = useTypesStore();
const router = useRouter();

const addDay = (iso) => {
  const date = new Date(iso);
  date.setDate(date.getDate() + 1);
  return date.toISOString().slice(0, 10);
};

const calendarEvents = computed(() =>
  plans.items.map((plan) => {
    const type = types.byId[plan.typeId];
    const color = type?.color || "#0f7a6c";
    return {
      id: plan.id,
      title: plan.title,
      start: plan.startsAt,
      end: plan.allDay ? addDay(plan.endsAt) : plan.endsAt,
      allDay: plan.allDay,
      backgroundColor: color,
      borderColor: color,
    };
  })
);

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: "dayGridMonth",
  locale: locale.value === "en" ? enLocale : esLocale,
  headerToolbar: {
    left: "prev,next today",
    center: "title",
    right: "dayGridMonth,timeGridWeek,timeGridDay",
  },
  height: "auto",
  events: calendarEvents.value,
  eventClick: (info) => {
    router.push({ name: "plan-detail", params: { id: info.event.id } });
  },
  dateClick: (info) => {
    router.push({
      name: "plan-new",
      query: { date: info.dateStr },
    });
  },
}));

onMounted(async () => {
  plans.filters.status = "";
  plans.filters.typeId = "";
  await Promise.all([types.refresh(), plans.refresh()]);
});
</script>

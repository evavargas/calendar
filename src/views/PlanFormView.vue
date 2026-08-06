<template>
  <section>
    <header class="page-header">
      <div>
        <h1>{{ isEdit ? "Editar plan" : "Nuevo plan" }}</h1>
        <p>Título, tipo, descripción y cuándo. El color lo define el tipo.</p>
      </div>
    </header>

    <UiAlert v-if="error">
      {{ error }}
    </UiAlert>

    <form
      class="form-grid ui-surface"
      style="padding: 1.25rem"
      @submit.prevent="onSubmit"
    >
      <label class="ui-field">
        <span>Título</span>
        <input
          v-model.trim="form.title"
          required
          maxlength="120"
          name="title"
        >
      </label>

      <fieldset class="ui-field">
        <legend>Tipo</legend>
        <div
          class="type-chip-row"
          role="radiogroup"
          aria-label="Tipo de plan"
        >
          <UiChip
            v-for="type in types.items"
            :key="type.id"
            :color="type.color"
            :active="form.typeId === type.id"
            role="radio"
            @click="form.typeId = type.id"
          >
            {{ type.name }}
          </UiChip>
        </div>
        <input
          v-model="form.typeId"
          required
          tabindex="-1"
          class="sr-only"
          name="typeId"
        >
        <p
          v-if="!types.items.length"
          class="ui-field__hint"
        >
          Creá un tipo en Tipos antes de guardar.
        </p>
      </fieldset>

      <label class="ui-field">
        <span>Descripción</span>
        <textarea
          v-model.trim="form.description"
          name="description"
          maxlength="2000"
          placeholder="¿Para qué es este plan?"
        />
      </label>

      <label class="ui-field">
        <span>
          <input
            v-model="form.allDay"
            type="checkbox"
          >
          Todo el día
        </span>
      </label>

      <div class="form-row">
        <label class="ui-field">
          <span>Inicio</span>
          <input
            v-model="form.startsAt"
            :type="form.allDay ? 'date' : 'datetime-local'"
            required
            name="startsAt"
          >
        </label>
        <label class="ui-field">
          <span>Fin</span>
          <input
            v-model="form.endsAt"
            :type="form.allDay ? 'date' : 'datetime-local'"
            required
            name="endsAt"
          >
        </label>
      </div>

      <label
        v-if="isEdit"
        class="ui-field"
      >
        <span>Estado</span>
        <select v-model="form.status">
          <option value="planned">
            Planificado
          </option>
          <option value="done">
            Hecho
          </option>
          <option value="cancelled">
            Cancelado
          </option>
        </select>
      </label>

      <div class="page-actions">
        <UiButton
          variant="primary"
          type="submit"
          :disabled="saving"
        >
          {{ isEdit ? "Guardar cambios" : "Crear plan" }}
        </UiButton>
        <UiButton
          variant="ghost"
          :to="isEdit ? { name: 'plan-detail', params: { id: route.params.id } } : { name: 'today' }"
        >
          Cancelar
        </UiButton>
      </div>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { UiAlert, UiButton, UiChip } from "../components/ui";
import { usePlansStore } from "../stores/plans";
import { useTypesStore } from "../stores/types";

const route = useRoute();
const router = useRouter();
const plans = usePlansStore();
const types = useTypesStore();
const saving = ref(false);
const error = ref("");

const isEdit = computed(() => route.name === "plan-edit");

const toInputValue = (iso, allDay) => {
  if (!iso) return "";
  const date = new Date(iso);
  if (allDay) return date.toISOString().slice(0, 10);
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 16);
};

const fromInputValue = (value, allDay) => {
  if (allDay) return new Date(`${value}T00:00:00`).toISOString();
  return new Date(value).toISOString();
};

const form = reactive({
  title: "",
  typeId: "",
  description: "",
  allDay: false,
  startsAt: "",
  endsAt: "",
  status: "planned",
});

onMounted(async () => {
  await types.refresh();
  if (!isEdit.value) {
    form.typeId = types.items[0]?.id || "";
    const start = new Date();
    if (typeof route.query.date === "string" && route.query.date) {
      const [year, month, day] = route.query.date.split("-").map(Number);
      start.setFullYear(year, month - 1, day);
      start.setHours(9, 0, 0, 0);
    } else {
      start.setMinutes(0, 0, 0);
      start.setHours(start.getHours() + 1);
    }
    const end = new Date(start);
    end.setHours(end.getHours() + 1);
    form.startsAt = toInputValue(start.toISOString(), false);
    form.endsAt = toInputValue(end.toISOString(), false);
    return;
  }

  const plan = await plans.loadOne(route.params.id);
  form.title = plan.title;
  form.typeId = plan.typeId;
  form.description = plan.description || "";
  form.allDay = Boolean(plan.allDay);
  form.startsAt = toInputValue(plan.startsAt, plan.allDay);
  form.endsAt = toInputValue(plan.endsAt, plan.allDay);
  form.status = plan.status;
});

const onSubmit = async () => {
  error.value = "";
  const startsAt = fromInputValue(form.startsAt, form.allDay);
  const endsAt = fromInputValue(form.endsAt, form.allDay);
  if (new Date(endsAt) < new Date(startsAt)) {
    error.value = "El fin no puede ser anterior al inicio.";
    return;
  }

  const payload = {
    title: form.title,
    typeId: form.typeId,
    description: form.description,
    allDay: form.allDay,
    startsAt,
    endsAt,
    ...(isEdit.value ? { status: form.status } : {}),
  };

  saving.value = true;
  try {
    if (isEdit.value) {
      await plans.updatePlan(route.params.id, payload);
      router.push({ name: "plan-detail", params: { id: route.params.id } });
    } else {
      const created = await plans.createPlan(payload);
      router.push({ name: "plan-detail", params: { id: created.id } });
    }
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "No se pudo guardar.";
  } finally {
    saving.value = false;
  }
};
</script>

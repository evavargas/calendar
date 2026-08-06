<template>
  <section>
    <header class="page-header">
      <div>
        <h1>Tipos de plan</h1>
        <p>El color y la semántica viven acá. Los planes heredan el tipo.</p>
      </div>
    </header>

    <UiAlert v-if="error">
      {{ error }}
    </UiAlert>

    <form
      class="form-grid ui-surface"
      style="padding: 1.25rem; margin-bottom: 1.25rem"
      @submit.prevent="onCreate"
    >
      <div class="form-row">
        <label class="ui-field">
          <span>Nombre</span>
          <input
            v-model.trim="form.name"
            required
            maxlength="40"
            name="name"
          >
        </label>
        <label class="ui-field">
          <span>Color</span>
          <input
            v-model="form.color"
            type="color"
            required
            name="color"
          >
        </label>
      </div>
      <UiButton
        variant="primary"
        type="submit"
      >
        Agregar tipo
      </UiButton>
    </form>

    <div class="type-grid">
      <UiSurface
        v-for="type in types.items"
        :key="type.id"
        tag="article"
        class="type-card"
      >
        <div
          class="type-swatch"
          :style="{ background: type.color }"
        />
        <strong>{{ type.name }}</strong>
        <UiButton
          variant="ghost"
          type="button"
          @click="onRemove(type.id)"
        >
          Eliminar
        </UiButton>
      </UiSurface>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { UiAlert, UiButton, UiSurface } from "../components/ui";
import { useTypesStore } from "../stores/types";

const types = useTypesStore();
const error = ref("");
const form = reactive({
  name: "",
  color: "#0f7a6c",
});

onMounted(() => types.refresh());

const onCreate = async () => {
  error.value = "";
  try {
    await types.createType({ name: form.name, color: form.color });
    form.name = "";
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "No se pudo crear.";
  }
};

const onRemove = async (id) => {
  error.value = "";
  try {
    await types.removeType(id);
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : "No se pudo eliminar.";
  }
};
</script>

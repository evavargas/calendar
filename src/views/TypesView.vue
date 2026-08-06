<template>
  <section>
    <header class="page-header">
      <div>
        <h1>{{ t("types.title") }}</h1>
        <p>{{ t("types.subtitle") }}</p>
      </div>
    </header>

    <UiAlert v-if="error">
      {{ error }}
    </UiAlert>

    <form
      class="form-grid ui-surface types-form"
      @submit.prevent="onCreate"
    >
      <div class="form-row">
        <label class="ui-field">
          <span>{{ t("types.name") }}</span>
          <input
            v-model.trim="form.name"
            required
            maxlength="40"
            name="name"
          >
        </label>
        <label class="ui-field">
          <span>{{ t("types.color") }}</span>
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
        {{ t("types.create") }}
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
          {{ t("plan.delete") }}
        </UiButton>
      </UiSurface>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { UiAlert, UiButton, UiSurface } from "../components/ui";
import { useTypesStore } from "../stores/types";

const { t } = useI18n();
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

<style scoped>
.types-form {
  padding: var(--space-5);
  margin-bottom: var(--space-5);
}
</style>

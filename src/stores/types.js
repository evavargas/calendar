import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { typesApi } from "../services/api/types";

export const useTypesStore = defineStore("types", () => {
  const items = ref([]);
  const loading = ref(false);
  const error = ref("");

  const byId = computed(() =>
    Object.fromEntries(items.value.map((type) => [type.id, type]))
  );

  const refresh = async () => {
    loading.value = true;
    error.value = "";
    try {
      items.value = await typesApi.list();
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : "No se pudieron cargar los tipos.";
      throw cause;
    } finally {
      loading.value = false;
    }
  };

  const createType = async (payload) => {
    const created = await typesApi.create(payload);
    items.value = [...items.value, created].sort((a, b) => a.sortOrder - b.sortOrder);
    return created;
  };

  const updateType = async (id, payload) => {
    const updated = await typesApi.update(id, payload);
    items.value = items.value.map((item) => (item.id === id ? updated : item));
    return updated;
  };

  const removeType = async (id) => {
    await typesApi.remove(id);
    items.value = items.value.filter((item) => item.id !== id);
  };

  return {
    items,
    loading,
    error,
    byId,
    refresh,
    createType,
    updateType,
    removeType,
  };
});

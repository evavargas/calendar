import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { plansApi } from "../services/api/plans";
import { downloadBlob } from "../composables/format";

export const usePlansStore = defineStore("plans", () => {
  const items = ref([]);
  const current = ref(null);
  const loading = ref(false);
  const error = ref("");
  const filters = ref({ status: "", typeId: "" });
  const counts = ref({ planned: 0, done: 0, cancelled: 0, total: 0 });

  const upcoming = computed(() =>
    items.value.filter((plan) => plan.status === "planned")
  );

  const refreshCounts = async () => {
    const all = await plansApi.list({});
    counts.value = {
      planned: all.filter((plan) => plan.status === "planned").length,
      done: all.filter((plan) => plan.status === "done").length,
      cancelled: all.filter((plan) => plan.status === "cancelled").length,
      total: all.length,
    };
  };

  const refresh = async () => {
    loading.value = true;
    error.value = "";
    try {
      const [listed] = await Promise.all([
        plansApi.list({
          status: filters.value.status || undefined,
          typeId: filters.value.typeId || undefined,
        }),
        refreshCounts().catch(() => undefined),
      ]);
      items.value = listed;
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : "No se pudieron cargar los planes.";
      throw cause;
    } finally {
      loading.value = false;
    }
  };

  const listRange = async ({ from, to } = {}) =>
    plansApi.list({
      from: from || undefined,
      to: to || undefined,
    });

  const loadOne = async (id) => {
    loading.value = true;
    error.value = "";
    try {
      current.value = await plansApi.get(id);
      return current.value;
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : "No se pudo cargar el plan.";
      throw cause;
    } finally {
      loading.value = false;
    }
  };

  const createPlan = async (payload) => {
    const created = await plansApi.create(payload);
    await refresh();
    return created;
  };

  const updatePlan = async (id, payload) => {
    const updated = await plansApi.update(id, payload);
    items.value = items.value.map((item) => (item.id === id ? updated : item));
    if (current.value?.id === id) current.value = updated;
    await refreshCounts().catch(() => undefined);
    return updated;
  };

  const removePlan = async (id) => {
    await plansApi.remove(id);
    items.value = items.value.filter((item) => item.id !== id);
    if (current.value?.id === id) current.value = null;
    await refreshCounts().catch(() => undefined);
  };

  const exportIcs = async () => {
    const blob = await plansApi.exportIcs({
      status: filters.value.status || undefined,
      typeId: filters.value.typeId || undefined,
    });
    downloadBlob(blob, "kairo-planes.ics");
  };

  const exportPlanIcs = async (id) => {
    const blob = await plansApi.exportPlanIcs(id);
    downloadBlob(blob, `kairo-${id}.ics`);
  };

  return {
    items,
    current,
    loading,
    error,
    filters,
    counts,
    upcoming,
    refresh,
    refreshCounts,
    listRange,
    loadOne,
    createPlan,
    updatePlan,
    removePlan,
    exportIcs,
    exportPlanIcs,
  };
});

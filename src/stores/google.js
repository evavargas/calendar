import { ref } from "vue";
import { defineStore } from "pinia";
import { googleApi } from "../services/api/google";

export const useGoogleStore = defineStore("google", () => {
  const connected = ref(false);
  const scopes = ref([]);
  const loading = ref(false);
  const error = ref("");
  const lastPush = ref(null);

  const refresh = async () => {
    loading.value = true;
    error.value = "";
    try {
      const status = await googleApi.status();
      connected.value = Boolean(status.connected);
      scopes.value = status.scopes || [];
    } catch (cause) {
      error.value =
        cause instanceof Error ? cause.message : "No se pudo consultar Google.";
      throw cause;
    } finally {
      loading.value = false;
    }
  };

  const connectMock = async () => {
    const status = await googleApi.connectMock();
    connected.value = status.connected;
    scopes.value = status.scopes || [];
  };

  const disconnect = async () => {
    const status = await googleApi.disconnect();
    connected.value = status.connected;
    scopes.value = status.scopes || [];
  };

  const pushPlan = async (planId) => {
    loading.value = true;
    error.value = "";
    try {
      lastPush.value = await googleApi.pushPlan(planId);
      return lastPush.value;
    } catch (cause) {
      error.value =
        cause instanceof Error ? cause.message : "No se pudo enviar a Google.";
      throw cause;
    } finally {
      loading.value = false;
    }
  };

  return {
    connected,
    scopes,
    loading,
    error,
    lastPush,
    refresh,
    connectMock,
    disconnect,
    pushPlan,
  };
});

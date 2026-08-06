import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { authApi } from "../services/api/auth";

export const useAuthStore = defineStore("auth", () => {
  const user = ref(null);
  const loading = ref(false);
  const bootstrapped = ref(false);
  const error = ref("");

  const isAuthenticated = computed(() => Boolean(user.value));

  const bootstrap = async ({ force = false } = {}) => {
    if (bootstrapped.value && !force) return;
    loading.value = true;
    error.value = "";
    try {
      user.value = await authApi.me();
    } catch {
      user.value = null;
    } finally {
      loading.value = false;
      bootstrapped.value = true;
    }
  };

  const completeMockLogin = async () => {
    loading.value = true;
    error.value = "";
    try {
      user.value = await authApi.completeMockLogin();
      return user.value;
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : "No se pudo iniciar sesión.";
      throw cause;
    } finally {
      loading.value = false;
    }
  };

  const logout = async () => {
    loading.value = true;
    error.value = "";
    try {
      await authApi.logout();
      user.value = null;
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : "No se pudo cerrar sesión.";
      throw cause;
    } finally {
      loading.value = false;
    }
  };

  return {
    user,
    loading,
    bootstrapped,
    error,
    isAuthenticated,
    bootstrap,
    completeMockLogin,
    logout,
  };
});

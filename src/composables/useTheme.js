import { computed, onMounted, onUnmounted, ref } from "vue";

const STORAGE_KEY = "kairo-theme";

export const THEME_LIGHT = "light";
export const THEME_DARK = "dark";

const isTheme = (value) => value === THEME_LIGHT || value === THEME_DARK;

export const getSystemTheme = () => {
  if (typeof window === "undefined" || !window.matchMedia) return THEME_LIGHT;
  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? THEME_DARK
    : THEME_LIGHT;
};

export const readStoredTheme = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return isTheme(stored) ? stored : null;
  } catch {
    return null;
  }
};

export const resolveTheme = () => readStoredTheme() || getSystemTheme();

export const applyTheme = (theme) => {
  const next = isTheme(theme) ? theme : THEME_LIGHT;
  document.documentElement.dataset.theme = next;
  const meta = document.querySelector('meta[name="theme-color"]');
  if (meta) {
    meta.setAttribute("content", next === THEME_DARK ? "#0d1218" : "#0f7a6c");
  }
  return next;
};

export const persistTheme = (theme) => {
  const next = applyTheme(theme);
  try {
    localStorage.setItem(STORAGE_KEY, next);
  } catch {
    /* ignore quota / private mode */
  }
  return next;
};

/** Call before Vue mount to avoid FOUC. */
export const bootTheme = () => applyTheme(resolveTheme());

export const useTheme = () => {
  const theme = ref(resolveTheme());

  const isDark = computed(() => theme.value === THEME_DARK);

  const setTheme = (next) => {
    theme.value = persistTheme(next);
  };

  const toggleTheme = () => {
    setTheme(theme.value === THEME_DARK ? THEME_LIGHT : THEME_DARK);
  };

  let media;
  const onSystemChange = (event) => {
    if (readStoredTheme()) return;
    theme.value = applyTheme(event.matches ? THEME_DARK : THEME_LIGHT);
  };

  onMounted(() => {
    theme.value = applyTheme(resolveTheme());
    media = window.matchMedia("(prefers-color-scheme: dark)");
    media.addEventListener("change", onSystemChange);
  });

  onUnmounted(() => {
    media?.removeEventListener("change", onSystemChange);
  });

  return {
    theme,
    isDark,
    setTheme,
    toggleTheme,
    THEME_LIGHT,
    THEME_DARK,
  };
};

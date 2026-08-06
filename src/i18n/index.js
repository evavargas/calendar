import { createI18n } from "vue-i18n";
import en from "./locales/en";
import es from "./locales/es";

export const LOCALE_STORAGE_KEY = "kairo-locale";
export const SUPPORTED_LOCALES = ["es", "en"];

export const dateLocales = {
  es: "es-AR",
  en: "en-US",
};

export const resolveInitialLocale = () => {
  if (typeof localStorage !== "undefined") {
    const saved = localStorage.getItem(LOCALE_STORAGE_KEY);
    if (SUPPORTED_LOCALES.includes(saved)) return saved;
  }
  if (typeof navigator !== "undefined") {
    const nav = (navigator.language || "es").slice(0, 2).toLowerCase();
    if (SUPPORTED_LOCALES.includes(nav)) return nav;
  }
  return "es";
};

export const i18n = createI18n({
  legacy: false,
  locale: resolveInitialLocale(),
  fallbackLocale: "es",
  messages: { es, en },
});

export const applyDocumentLocale = (locale) => {
  if (typeof document === "undefined") return;
  document.documentElement.lang = locale === "en" ? "en" : "es";
};

applyDocumentLocale(i18n.global.locale.value);

export default i18n;

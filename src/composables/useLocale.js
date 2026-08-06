import { computed } from "vue";
import { useI18n } from "vue-i18n";
import {
  LOCALE_STORAGE_KEY,
  SUPPORTED_LOCALES,
  applyDocumentLocale,
  dateLocales,
} from "../i18n";

export const useLocale = () => {
  const { locale, t } = useI18n();

  const dateLocale = computed(() => dateLocales[locale.value] || dateLocales.es);

  const setLocale = (next) => {
    if (!SUPPORTED_LOCALES.includes(next)) return;
    locale.value = next;
    localStorage.setItem(LOCALE_STORAGE_KEY, next);
    applyDocumentLocale(next);
  };

  return {
    locale,
    dateLocale,
    setLocale,
    t,
    supportedLocales: SUPPORTED_LOCALES,
  };
};

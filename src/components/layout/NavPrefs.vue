<template>
  <div class="nav-prefs">
    <div
      class="nav-prefs__group"
      role="group"
      :aria-label="t('settings.theme')"
    >
      <button
        type="button"
        class="nav-prefs__btn"
        :data-active="theme === THEME_LIGHT || undefined"
        :aria-pressed="theme === THEME_LIGHT"
        @click="setTheme(THEME_LIGHT)"
      >
        {{ t("settings.light") }}
      </button>
      <button
        type="button"
        class="nav-prefs__btn"
        :data-active="theme === THEME_DARK || undefined"
        :aria-pressed="theme === THEME_DARK"
        @click="setTheme(THEME_DARK)"
      >
        {{ t("settings.dark") }}
      </button>
    </div>
    <div
      class="nav-prefs__group"
      role="group"
      :aria-label="t('settings.language')"
    >
      <button
        type="button"
        class="nav-prefs__btn"
        :data-active="locale === 'es' || undefined"
        :aria-pressed="locale === 'es'"
        @click="setLocale('es')"
      >
        ES
      </button>
      <button
        type="button"
        class="nav-prefs__btn"
        :data-active="locale === 'en' || undefined"
        :aria-pressed="locale === 'en'"
        @click="setLocale('en')"
      >
        EN
      </button>
    </div>
  </div>
</template>

<script setup>
import { THEME_DARK, THEME_LIGHT, useTheme } from "../../composables/useTheme";
import { useLocale } from "../../composables/useLocale";

const { theme, setTheme } = useTheme();
const { t, locale, setLocale } = useLocale();
</script>

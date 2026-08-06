<template>
  <a
    class="skip-link"
    href="#main"
  >{{ t("app.skipToContent") }}</a>
  <RouterView />
</template>

<script setup>
import { RouterView } from "vue-router";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

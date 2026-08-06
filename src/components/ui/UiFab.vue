<template>
  <RouterLink
    class="ui-fab"
    :to="to"
    :aria-label="ariaLabel"
  >
    <slot>+</slot>
  </RouterLink>
</template>

<script setup>
import { RouterLink } from "vue-router";

defineProps({
  to: { type: [String, Object], required: true },
  ariaLabel: { type: String, default: "Nuevo" },
});
</script>

<template>
  <span
    class="ui-badge"
    :class="toneClass"
  >
    <span
      v-if="dot"
      class="ui-badge__dot"
      :style="{ background: typeof dot === 'string' ? dot : undefined }"
      aria-hidden="true"
    />
    <slot />
  </span>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  /** true = accent dot; string = custom color */
  dot: { type: [Boolean, String], default: false },
  /** planned | done | cancelled | default */
  tone: { type: String, default: "default" },
});

const toneClass = computed(() => {
  if (props.tone === "planned") return "ui-badge--planned";
  if (props.tone === "done") return "ui-badge--done";
  if (props.tone === "cancelled") return "ui-badge--cancelled";
  return null;
});
</script>

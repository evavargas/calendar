<template>
  <span class="ui-badge">
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
defineProps({
  /** true = accent dot; string = custom color */
  dot: { type: [Boolean, String], default: false },
});
</script>

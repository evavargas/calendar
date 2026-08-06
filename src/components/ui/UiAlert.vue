<template>
  <p
    class="ui-alert"
    :class="{ 'ui-alert--info': tone === 'info' }"
    :role="tone === 'info' ? 'status' : 'alert'"
  >
    <slot />
  </p>
</template>

<script setup>
defineProps({
  tone: {
    type: String,
    default: "danger",
    validator: (value) => ["danger", "info"].includes(value),
  },
});
</script>

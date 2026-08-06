<template>
  <div class="ui-empty ui-surface">
    <h2
      v-if="title"
      class="ui-empty__title"
    >
      {{ title }}
    </h2>
    <div
      v-if="$slots.default"
      class="ui-empty__body"
    >
      <slot />
    </div>
    <div
      v-if="$slots.actions"
      class="ui-empty__actions"
    >
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: "" },
});
</script>

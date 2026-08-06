<template>
  <component
    :is="tag"
    class="ui-surface"
  >
    <slot />
  </component>
</template>

<script setup>
defineProps({
  tag: { type: String, default: "div" },
});
</script>

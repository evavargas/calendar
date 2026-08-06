<template>
  <component
    :is="isStatic ? 'span' : 'button'"
    :type="isStatic ? undefined : 'button'"
    class="ui-chip"
    :class="{
      'ui-chip--active': active,
      'ui-chip--static': isStatic,
    }"
    :style="chipStyle"
    :role="role"
    :aria-checked="role === 'radio' ? active : undefined"
    :aria-selected="role === 'option' ? active : undefined"
    @click="onClick"
  >
    <span
      v-if="showDot"
      class="ui-chip__dot"
      aria-hidden="true"
    />
    <slot />
  </component>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  color: { type: String, default: "" },
  active: { type: Boolean, default: false },
  isStatic: { type: Boolean, default: false },
  showDot: { type: Boolean, default: true },
  role: { type: String, default: null },
});

const emit = defineEmits(["click"]);

const chipStyle = computed(() => {
  const color = props.color || "var(--color-accent)";
  const style = { "--chip-color": color, borderColor: color };
  if (props.active) {
    style.background = `color-mix(in srgb, ${color} 28%, var(--color-surface))`;
  }
  return style;
});

const onClick = (event) => {
  if (props.isStatic) return;
  emit("click", event);
};
</script>

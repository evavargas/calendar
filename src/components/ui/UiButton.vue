<template>
  <component
    :is="componentTag"
    v-bind="linkProps"
    :class="['ui-btn', `ui-btn--${variant}`, className]"
    :type="isLink ? undefined : type"
    :disabled="isLink ? undefined : disabled"
    :href="href"
    @click="onClick"
  >
    <slot />
  </component>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink } from "vue-router";

const props = defineProps({
  variant: {
    type: String,
    default: "primary",
    validator: (value) =>
      ["primary", "secondary", "ghost", "danger", "hero-primary", "hero-secondary"].includes(
        value
      ),
  },
  to: { type: [String, Object], default: null },
  href: { type: String, default: null },
  type: { type: String, default: "button" },
  disabled: { type: Boolean, default: false },
  className: { type: [String, Array, Object], default: "" },
});

const emit = defineEmits(["click"]);

const isRouterLink = computed(() => Boolean(props.to));
const isAnchor = computed(() => Boolean(props.href) && !props.to);
const isLink = computed(() => isRouterLink.value || isAnchor.value);

const componentTag = computed(() => {
  if (isRouterLink.value) return RouterLink;
  if (isAnchor.value) return "a";
  return "button";
});

const linkProps = computed(() => {
  if (isRouterLink.value) return { to: props.to };
  return {};
});

const onClick = (event) => {
  if (props.disabled && isLink.value) {
    event.preventDefault();
    return;
  }
  emit("click", event);
};
</script>

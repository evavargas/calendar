<template>
  <article class="plan-card ui-surface">
    <div
      class="type-swatch"
      :style="{ background: typeColor, height: '100%', width: '6px', borderRadius: '999px' }"
      aria-hidden="true"
    />
    <div>
      <h2>{{ plan.title }}</h2>
      <p>{{ formatPlanWhen(plan) }}</p>
      <div class="plan-meta">
        <UiChip
          :color="typeColor"
          is-static
        >
          {{ typeName }}
        </UiChip>
        <UiBadge>{{ statusLabel(plan.status) }}</UiBadge>
      </div>
      <p
        v-if="plan.description"
        class="plan-card-desc"
      >
        {{ truncatedDescription }}
      </p>
    </div>
    <div aria-hidden="true">
      →
    </div>
  </article>
</template>

<script setup>
import { computed } from "vue";
import { UiBadge, UiChip } from "../ui";
import { formatPlanWhen, statusLabel } from "../../composables/format";
import { useTypesStore } from "../../stores/types";

const props = defineProps({
  plan: { type: Object, required: true },
});

const types = useTypesStore();
const type = computed(() => types.byId[props.plan.typeId]);
const typeName = computed(() => type.value?.name || "Sin tipo");
const typeColor = computed(() => type.value?.color || "#0f7a6c");
const truncatedDescription = computed(() => {
  const text = props.plan.description || "";
  return text.length > 110 ? `${text.slice(0, 110)}…` : text;
});
</script>

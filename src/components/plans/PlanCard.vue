<template>
  <article class="plan-card ui-surface">
    <div
      class="plan-card-swatch"
      :style="{ background: typeColor }"
      aria-hidden="true"
    />
    <div>
      <h2>{{ plan.title }}</h2>
      <p>{{ whenLabel }}</p>
      <div class="plan-meta">
        <UiChip
          :color="typeColor"
          is-static
        >
          {{ typeName }}
        </UiChip>
        <UiBadge :tone="plan.status">
          {{ t(statusKey(plan.status)) }}
        </UiBadge>
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
import { useI18n } from "vue-i18n";
import { UiBadge, UiChip } from "../ui";
import { formatPlanWhen, statusKey } from "../../composables/format";
import { useLocale } from "../../composables/useLocale";
import { useTypesStore } from "../../stores/types";

const props = defineProps({
  plan: { type: Object, required: true },
});

const { t } = useI18n();
const { dateLocale } = useLocale();
const types = useTypesStore();
const type = computed(() => types.byId[props.plan.typeId]);
const typeName = computed(() => type.value?.name || t("plan.noType"));
const typeColor = computed(() => type.value?.color || "#0f7a6c");
const whenLabel = computed(() => formatPlanWhen(props.plan, dateLocale.value));
const truncatedDescription = computed(() => {
  const text = props.plan.description || "";
  return text.length > 110 ? `${text.slice(0, 110)}…` : text;
});
</script>

<style scoped>
.plan-card-swatch {
  width: 6px;
  height: 100%;
  min-height: 3rem;
  border-radius: var(--radius-xs);
  align-self: stretch;
}
</style>

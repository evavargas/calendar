import { apiRequest, isMockApi } from "./client";
import { mockIcs, mockPlans } from "../../mocks/store";

const toQuery = (filters = {}) => {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value != null && value !== "") params.set(key, value);
  });
  const query = params.toString();
  return query ? `?${query}` : "";
};

export const plansApi = {
  list(filters) {
    return isMockApi()
      ? mockPlans.list(filters)
      : apiRequest(`/api/plans${toQuery(filters)}`);
  },
  get(id) {
    return isMockApi() ? mockPlans.get(id) : apiRequest(`/api/plans/${id}`);
  },
  create(payload) {
    return isMockApi()
      ? mockPlans.create(payload)
      : apiRequest("/api/plans", {
          method: "POST",
          body: JSON.stringify(payload),
        });
  },
  update(id, payload) {
    return isMockApi()
      ? mockPlans.update(id, payload)
      : apiRequest(`/api/plans/${id}`, {
          method: "PATCH",
          body: JSON.stringify(payload),
        });
  },
  remove(id) {
    return isMockApi()
      ? mockPlans.remove(id)
      : apiRequest(`/api/plans/${id}`, { method: "DELETE" });
  },
  async exportIcs(filters) {
    if (isMockApi()) {
      const plans = await mockPlans.list(filters);
      return mockIcs.exportPlans(plans);
    }
    return apiRequest(`/api/plans/export.ics${toQuery(filters)}`);
  },
  async exportPlanIcs(id) {
    if (isMockApi()) {
      const plan = await mockPlans.get(id);
      return mockIcs.exportPlans([plan]);
    }
    return apiRequest(`/api/plans/${id}/ics`);
  },
};

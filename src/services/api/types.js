import { apiRequest, isMockApi } from "./client";
import { mockTypes } from "../../mocks/store";

export const typesApi = {
  list() {
    return isMockApi() ? mockTypes.list() : apiRequest("/api/types");
  },
  create(payload) {
    return isMockApi()
      ? mockTypes.create(payload)
      : apiRequest("/api/types", {
          method: "POST",
          body: JSON.stringify(payload),
        });
  },
  update(id, payload) {
    return isMockApi()
      ? mockTypes.update(id, payload)
      : apiRequest(`/api/types/${id}`, {
          method: "PATCH",
          body: JSON.stringify(payload),
        });
  },
  remove(id) {
    return isMockApi()
      ? mockTypes.remove(id)
      : apiRequest(`/api/types/${id}`, { method: "DELETE" });
  },
};

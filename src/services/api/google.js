import { apiRequest, isMockApi } from "./client";
import { mockGoogle } from "../../mocks/store";

export const googleApi = {
  status() {
    return isMockApi() ? mockGoogle.status() : apiRequest("/api/google/status");
  },
  connectMock() {
    if (!isMockApi()) {
      throw new Error("connectMock solo está disponible en mock API");
    }
    return mockGoogle.connect();
  },
  disconnect() {
    return isMockApi()
      ? mockGoogle.disconnect()
      : apiRequest("/api/google/connect", { method: "DELETE" });
  },
  pushPlan(planId) {
    return isMockApi()
      ? mockGoogle.pushPlan(planId)
      : apiRequest(`/api/plans/${planId}/google`, { method: "POST" });
  },
};

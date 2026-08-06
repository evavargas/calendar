import { apiRequest, isMockApi } from "./client";
import { mockAuth } from "../../mocks/store";

export const authApi = {
  me() {
    return isMockApi() ? mockAuth.me() : apiRequest("/api/auth/me");
  },
  logout() {
    return isMockApi()
      ? mockAuth.logout()
      : apiRequest("/api/auth/logout", { method: "POST" });
  },
  /** Solo mock: simula el callback OAuth de Google. */
  completeMockLogin() {
    if (!isMockApi()) {
      throw new Error("completeMockLogin solo está disponible en mock API");
    }
    return mockAuth.loginWithGoogle();
  },
};

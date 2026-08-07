const USE_MOCK = import.meta.env.VITE_USE_MOCK_API !== "false";
const API_BASE = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");

export class ApiError extends Error {
  constructor(message, { status = 500, code = "api_error" } = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
  }
}

const parseError = async (response) => {
  try {
    const payload = await response.json();
    return (
      payload?.error?.message ||
      payload?.message ||
      `Error ${response.status}`
    );
  } catch {
    return `Error ${response.status}`;
  }
};

export const apiRequest = async (path, options = {}) => {
  if (USE_MOCK) {
    throw new ApiError("Mock mode: use mock modules instead of apiRequest", {
      status: 500,
      code: "mock_miswired",
    });
  }

  const response = await fetch(`${API_BASE}${path}`, {
    credentials: "include",
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers,
    },
    ...options,
  });

  if (response.status === 204 || response.status === 205) {
    return null;
  }

  if (!response.ok) {
    throw new ApiError(await parseError(response), {
      status: response.status,
      code: "http_error",
    });
  }

  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("text/calendar")) {
    return response.blob();
  }

  // Empty bodies (some proxies) should not blow up JSON.parse via response.json()
  const text = await response.text();
  if (!text) {
    return null;
  }
  if (contentType.includes("application/json") || text.startsWith("{") || text.startsWith("[")) {
    return JSON.parse(text);
  }
  return text;
};

export const isMockApi = () => USE_MOCK;

export const authLoginUrl = () =>
  USE_MOCK
    ? "/auth/callback?mock=1"
    : // Same-origin via Vercel rewrite — never bounce through the Cloud Run host
      // or the OAuth cookie is set on *.run.app and lost on callback.
      "/oauth2/authorization/google";

export const googleConnectUrl = () =>
  USE_MOCK ? "/app/settings?google=connected" : `${API_BASE}/api/google/connect`;

/** Full-page navigation for OAuth redirects (keeps cookies / proxy path). */
export const navigateToAuthLogin = () => {
  window.location.assign(authLoginUrl());
};

export const navigateToGoogleConnect = () => {
  window.location.assign(googleConnectUrl());
};

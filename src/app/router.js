import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      name: "landing",
      component: () => import("../views/LandingView.vue"),
      meta: { guestOnly: true },
    },
    {
      path: "/auth/callback",
      name: "auth-callback",
      component: () => import("../views/AuthCallbackView.vue"),
    },
    {
      path: "/app",
      component: () => import("../components/layout/AppShell.vue"),
      meta: { requiresAuth: true },
      children: [
        {
          path: "",
          name: "today",
          component: () => import("../views/TodayView.vue"),
        },
        {
          path: "plans",
          name: "planner",
          component: () => import("../views/PlannerView.vue"),
        },
        {
          path: "plans/new",
          name: "plan-new",
          component: () => import("../views/PlanFormView.vue"),
        },
        {
          path: "plans/:id",
          name: "plan-detail",
          component: () => import("../views/PlanDetailView.vue"),
        },
        {
          path: "plans/:id/edit",
          name: "plan-edit",
          component: () => import("../views/PlanFormView.vue"),
        },
        {
          path: "types",
          name: "types",
          component: () => import("../views/TypesView.vue"),
        },
        {
          path: "calendar",
          name: "calendar",
          component: () => import("../views/CalendarView.vue"),
        },
        {
          path: "settings",
          name: "settings",
          component: () => import("../views/SettingsView.vue"),
        },
      ],
    },
    {
      path: "/:pathMatch(.*)*",
      redirect: "/",
    },
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (!auth.bootstrapped) {
    await auth.bootstrap();
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: "landing", query: { next: to.fullPath } };
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: "today" };
  }

  return true;
});

export default router;

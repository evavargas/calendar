import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./app/App.vue";
import router from "./app/router";
import i18n from "./i18n";
import { bootTheme } from "./composables/useTheme";
import "./styles/index.css";

bootTheme();

createApp(App).use(createPinia()).use(router).use(i18n).mount("#app");

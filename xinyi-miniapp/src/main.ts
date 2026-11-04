// 应用入口仅装配Vue与状态容器，账号身份和工作权限由API会话恢复。
import { createSSRApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
export function createApp() {
  const app = createSSRApp(App);
  app.use(createPinia());
  return { app };
}

/// <reference types="vite/client" />
// 仅提供构建期模块类型声明，不影响运行时权限或身份。
declare module '*.vue' {
  import { DefineComponent } from 'vue';
  const component: DefineComponent;
  export default component;
}

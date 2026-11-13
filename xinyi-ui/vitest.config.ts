import { defineConfig } from 'vitest/config'

// 只跑纯函数单测：不加载 uni/vite 插件，避免构建链副作用，也快
export default defineConfig({
  test: {
    environment: 'node',
    include: ['src/**/*.spec.ts'],
    reporters: ['default']
  }
})

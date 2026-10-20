# E05 · PC框架接入和双域API客户端

阶段：M0；当前用户未实施M0。前置：E01。

## 本任务交付

- 复用xinyi-ui布局与权限路由，新增vi请求适配而不改旧接口封装

## 代码落位

`docs/implementation/vehicle/E05/`  

## 失败与边界

- 根路径/api只出现一次
- 不覆盖原Jwt响应与错误处理

## 验证命令

```text
npm --prefix xinyi-ui ci
npm --prefix xinyi-ui run build:prod
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- 保留原PC框架行为并完成新请求适配的组件测试及生产构建
- 尚未实现的业务页面由后续各任务验收，不生成假Ack跳过
- 保存实际日志和独立复核

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。

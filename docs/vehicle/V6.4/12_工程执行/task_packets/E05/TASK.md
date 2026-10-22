# E05 · PC框架接入和双域API客户端

阶段：M0；责任域：web；前置：E01。
范围版本：`V6.1-UNCHANGED`；definitionHash：`e7983fb6fee4fcd46a7ddc0cd8d731eb50f50d254d3775e976b1bb18b223bc43`。

## 本任务交付
- 复用xinyi-ui布局与权限路由，新增vi请求适配而不改旧接口封装

## 必测失败与并发
- 根路径/api只出现一次
- 不覆盖原Jwt响应与错误处理

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/E05/`

## 验证命令
```bash
npm --prefix xinyi-ui ci
npm --prefix xinyi-ui run build:prod
```

## 完成标准
- 保留原PC框架行为并完成新请求适配的组件测试及生产构建
- 尚未实现的业务页面由后续各任务验收，不生成假Ack跳过
- 保存实际日志和独立复核

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。

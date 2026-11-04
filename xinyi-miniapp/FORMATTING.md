# 类型文件排版

`src/api/types.ts` 是由历史资料生成、随后按当前契约适配的HTTP传输类型。同步类型后执行：

```sh
npm run format:types
npm run format:types:check
npm run type-check
npm run build:mp-weixin
```

格式工具固定为开发依赖Prettier 3.6.2，安装使用仓库package-lock.json。采用两空格、单引号和100字符参考行宽；格式命令只改排版，不更改类型或接口含义。

当前仓库未发现由主OpenAPI完整重生成此文件的可执行入口，因此本轮没有声称重新生成全部契约类型，也不引入未经验证的替代生成器。将来补充生成步骤时，必须在生成后调用同一排版/检查命令，并继续运行契约与类型检查。

Vue页面应展开模板与样式，权限、会话失效、幂等重试等不直观逻辑写中文原因说明。处理模板中的TypeScript表达式时保留表达式内容并验证编译，不能为适配通用HTML格式器删除类型约束或改变逻辑。

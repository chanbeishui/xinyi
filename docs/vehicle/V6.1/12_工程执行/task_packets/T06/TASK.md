# T06 · 结构、Mapper生成列和升级一致性

阶段：M0；当前用户未实施M0。前置：E03, E06。

## 本任务交付

- 真实MySQL执行66表、主键/索引/CHECK/UTC/自增/生成列
- 初装与M0适配目标一致

## 代码落位

`docs/implementation/vehicle/T06/`  

## 失败与边界

- H2/SQLite不能替代此门禁
- SQL可解析不等于可执行

## 验证命令

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=T06TestingIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- 源接口/页面/表/用例均有明确实现文件与关联测试；不能以目录存在当完成
- 本任务真实集成与负向场景实际通过且保存原始报告；无测试/跳过不能通过
- 提交独立复核记录；标注测试第三方替身，不声称真实渠道通过

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。

# N02 · 隐私、公开内容和设置

阶段：M4；主责任域：privacy；前置：C01, I01。

## 本任务交付

- 公开企业条款、版本授权、显示偏好、隐私请求进度

## 必测失败与并发

- 旧保险钱包文案不回流
- 不物理删资金交接证据
- 权限撤回不抹历史

## 精确输入

主契约：`04_接口/openapi_V6.0.yaml`；本目录切片是自动派生，不单独修改。

接口：API_153_get, API_154_post, API_163_post, API_164_get

画板：U03, U03-progress, U05, U06, O09, O24, O28, D26, D30, S20, B34

建模对象：复用前置任务表；不因此新增无用表

源验收：TC-193, TC-194

## 代码落位

`xinyi-rongli/src/main/java/com/xinyi/rongli/privacy/`  
`xinyi-rongli/src/main/resources/mapper/rongli/privacy/`  
`xinyi-admin/src/main/java/com/xinyi/web/controller/rongli/privacy/`  
`xinyi-ui/src/views/rongli/privacy/`  
`xinyi-ui/src/api/rongli/privacy.ts`  
`xinyi-miniapp/src/features/privacy/`  

实际模块如已存在其他路径，以E00记录为准；不得并行建第二套vehicle/rongli模块。每一个涉及视图均实现PC或uni-app真实接口，不复制原型内存模型。

## 验证

```text
mvn -pl xinyi-admin -am -Pvi-it -Dit.test=N02PrivacyIT verify
npm --prefix xinyi-ui run build:prod
npm --prefix xinyi-miniapp run build:mp-weixin
```

上述任务级IT类由本任务实现；不得创建空类/忽略失败来通过。执行前加载 .env.vi.local 和 vi-it 配置，公共启动方式见13_真实集成测试。

- 后端业务必须真实HTTP→Service→Mapper→MySQL，并验证持久化、失败与权限边界。
- 无测试、跳过、固定Ack、原型内存交互均不能作为业务完成。
- 保存具体代码路径、原始报告、测试数与独立复核；依赖缺失只阻塞相应验证。

## 完成后的执行

把状态和证据写入工作区任务账本；调用taskctl next选择依赖就绪项，不停留于“建议下一步”。运行预算耗尽时更新RESUME.md，不丢弃修改。

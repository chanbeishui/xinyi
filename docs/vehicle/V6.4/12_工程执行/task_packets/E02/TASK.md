# E02 · 隔离运行环境与依赖锁定

阶段：M0；责任域：platform；前置：E00。
范围版本：`V6.1-UNCHANGED`；definitionHash：`10409ba525f5bbe5efab70aafb706d4b09f144d7dfb750c7d89b2ae7cdd078de`。

## 本任务交付
- 独立MySQL/Redis/文件目录/模拟供应商
- JDK21运行release17
- 锁定包与镜像版本

## 必测失败与并发
- 生产URL和凭据不加载
- 镜像未就绪不得算启动成功
- Docker不可用有独立测试实例路径

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/E02/`

## 验证命令
```bash
python docs/vehicle/V6.4/12_工程执行/engineer.py env --repo . --apply
python docs/vehicle/V6.4/12_工程执行/engineer.py run --repo . --kind infra
python docs/vehicle/V6.4/12_工程执行/engineer.py run --repo . --kind infra-status
```

## 完成标准
- 隔离环境配置、随机本地密钥与端口不干扰原库
- 真实工具版本/拉取/健康检查有证据；失败部分保持VERIFICATION阻塞
- 不要求未开发的身份/报价/付款提前通过

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。

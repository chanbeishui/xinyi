# E01 · 契约修复和最小输入闭合

阶段：M0；责任域：contract；前置：E00。
范围版本：`V6.4-CONFIG-20261022`；definitionHash：`af3f11a8f76f785a371d036f356e36e0ba1391df975e1d2ff6227ce3c81d1f99`。

## 本任务交付
- 验证新/旧车辆与新/旧地址四组合报价，XOR、同意协议约束
- 生成切片契约和类型
- 审查身份相关枚举/权限清单/公开配置登记；旧凭证接口彻底退役
- 相关修订由主源重新派生，检查全部引用及正常/冲突样本

## 必测失败与并发
- 缺组、双传、null、ID溢出、伪造金额与站点必须拒绝

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/E01/`

## 验证命令
```bash
python docs/vehicle/V6.4/12_工程执行/engineer.py check --repo .
python -m pytest docs/vehicle/V6.4/12_工程执行/tests/test_contract_regression.py -q
```

## 完成标准
- 执行本包契约与报价回归，保存实际断言记录
- 当前静态契约检查不标真实API已实现
- 继续环境/工程建设，不以再次设计整包作为结束

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。

# E01 · 契约修复和最小输入闭合

阶段：M0；当前用户未实施M0。前置：E00。

## 本任务交付

- 验证新/旧车辆与新/旧地址四组合报价，XOR、同意协议约束
- 生成切片契约和类型

## 代码落位

`docs/implementation/vehicle/E01/`  

## 失败与边界

- 缺组、双传、null、ID溢出、伪造金额与站点必须拒绝

## 验证命令

```text
python docs/vehicle/V6.1/12_工程执行/engineer.py check --repo .
python -m pytest docs/vehicle/V6.1/12_工程执行/tests/test_contract_regression.py -q
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- 执行本包契约与报价回归，保存实际断言记录
- 当前静态契约检查不标真实API已实现
- 继续环境/工程建设，不以再次设计整包作为结束

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。

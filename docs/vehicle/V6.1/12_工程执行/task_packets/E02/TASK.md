# E02 · 隔离运行环境与依赖锁定

阶段：M0；当前用户未实施M0。前置：E00。

## 本任务交付

- 独立MySQL/Redis/文件目录/模拟供应商
- JDK21运行release17
- 锁定包与镜像版本

## 代码落位

`docs/implementation/vehicle/E02/`  

## 失败与边界

- 生产URL和凭据不加载
- 镜像未就绪不得算启动成功
- Docker不可用有独立测试实例路径

## 验证命令

```text
python docs/vehicle/V6.1/12_工程执行/engineer.py env --repo . --apply
python docs/vehicle/V6.1/12_工程执行/engineer.py run --repo . --kind infra
python docs/vehicle/V6.1/12_工程执行/engineer.py run --repo . --kind infra-status
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- 隔离环境配置、随机本地密钥与端口不干扰原库
- 真实工具版本/拉取/健康检查有证据；失败部分保持VERIFICATION阻塞
- 不要求未开发的身份/报价/付款提前通过

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。

# E08 · CI、任务台账与证据收集

阶段：M0；当前用户未实施M0。前置：E01。

## 本任务交付

- 跨平台任务命令、接口页面表用例覆盖、JUnit报告、失败退出码

## 代码落位

`docs/implementation/vehicle/E08/`  

## 失败与边界

- 不允许NOT_RUN/skip算通过
- 无测试/无实体行为不得标DONE

## 验证命令

```text
python -m pytest docs/vehicle/V6.1/12_工程执行/tests -q
python docs/vehicle/V6.1/12_工程执行/engineer.py check --repo .
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- CI/任务/证据工具在受控测试工作区验证，阶段从FOUNDATION开始
- 未实跑的数据库、HTTP、真机明确NOT_RUN
- 测试阶段按后续能力升级，不用FOUNDATION绿色代替全功能

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。

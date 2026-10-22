# E08 · CI、任务台账与证据收集

阶段：M0；责任域：platform；前置：E01。
范围版本：`V6.1-UNCHANGED`；definitionHash：`db8eb6256d8dd894ca3f992aa695048adc006227df1c792cec239d39a3a27fd4`。

## 本任务交付
- 跨平台任务命令、接口页面表用例覆盖、JUnit报告、失败退出码

## 必测失败与并发
- 不允许NOT_RUN/skip算通过
- 无测试/无实体行为不得标DONE

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`docs/implementation/vehicle/E08/`

## 验证命令
```bash
python -m pytest docs/vehicle/V6.4/12_工程执行/tests -q
python docs/vehicle/V6.4/12_工程执行/engineer.py check --repo .
```

## 完成标准
- CI/任务/证据工具在受控测试工作区验证，阶段从FOUNDATION开始
- 未实跑的数据库、HTTP、真机明确NOT_RUN
- 测试阶段按后续能力升级，不用FOUNDATION绿色代替全功能

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。

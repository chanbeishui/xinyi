# E00 · 框架基线核对：M0尚未开始

阶段：M0；当前用户未实施M0。前置：无。

## 本任务交付

- 记录当前rongli-wx工作区HEAD、branch、dirty、有效AGENTS与源码指纹；用户未做M0
- 确认框架模块并记录将新建xinyi-rongli与xinyi-miniapp；不从旧日志推断完成
- 保留sys和其他框架业务；不checkout/reset/clean，不重启或改写原数据库

## 代码落位

`docs/implementation/vehicle/E00/`  

## 失败与边界

- commit标题/目录存在/SQL文件不构成M0通过
- 当前HEAD不同只登记，不自动回退
- 数据库状态未知不能默认删除或迁移

## 验证命令

```text
python docs/vehicle/V6.1/12_工程执行/engineer.py doctor --repo .
python docs/vehicle/V6.1/12_工程执行/taskctl.py summary --repo .
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- 当前工作区观察报告有真实命令、HEAD与差异；无密钥和个人数据
- 只完成E00基线，不预标M0整体、构建或业务测试通过
- 独立复核观察证据，随后继续E01/E02等就绪任务

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。

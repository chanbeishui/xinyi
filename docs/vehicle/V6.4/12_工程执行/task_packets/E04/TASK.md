# E04 · 新建业务Maven模块与uni-app工程

阶段：M0；责任域：mobile；前置：E01。
范围版本：`V6.4-IDENTITY-20261022`；definitionHash：`740a2401e00295b94a693a7466a1caeea554bfd68bfbf6633366d26af0123fef`。

## 本任务交付
- 先检查并复用已有业务模块和小程序；不存在才新建并接根POM
- 保持Node22项目范围和真实lockfile；不得覆写本地改名
- 新身份默认OWNER与服务端返回能力联动

## 必测失败与并发
- 无DOM移植
- 不以H5成功等同微信通过
- 存量工程不覆盖

## 精确输入
主源：04_接口/openapi_V6.4.yaml、05_数据设计/data_model_V6.4.json、06_验收/test_cases_V6.4.json。本目录派生，不独立维护。
接口：无
画板：无
主责任表：无
验收：无

## 代码落位
`pom.xml`
`xinyi-admin/pom.xml`
`xinyi-rongli/pom.xml`
`xinyi-rongli/src/main/`
`xinyi-miniapp/`

## 验证命令
```bash
python docs/vehicle/V6.4/12_工程执行/scaffold.py --repo .
python docs/vehicle/V6.4/12_工程执行/scaffold.py --repo . --apply
npm --prefix xinyi-miniapp install
npm --prefix xinyi-miniapp run type-check
npm --prefix xinyi-miniapp run build:mp-weixin
python docs/vehicle/V6.4/12_工程执行/engineer.py run --repo . --kind backend-build
```

## 完成标准
- 新模块与小程序实际编译；lockfile来自真实安装而非猜测
- 模板安装不覆盖框架/已有源码；没有数据库的纯构建不标业务通过
- 保存编译日志与独立复核，后续使用npm ci

## 执行边界
已有实现先核查适配，不清空重来。NOT_RUN与NOT_ASSESSED不表示生产已验证。缺真实凭据只阻对应真实联调；环境阻塞如实记录，不改测试期望凑通过。修改主源后重新生成派生文件和当前截图。

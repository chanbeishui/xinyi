# E04 · 新建业务Maven模块与uni-app工程

阶段：M0；当前用户未实施M0。前置：E01。

## 本任务交付

- 新建xinyi-rongli模块并接入根POM/admin依赖；保留现有六个框架模块
- 新建xinyi-miniapp Vue3/TS并实际生成lockfile，编译微信产物
- 请求、单在途刷新、视角上下文和错误处理基础；后续业务实现另走任务

## 代码落位

`pom.xml`  
`xinyi-admin/pom.xml`  
`xinyi-rongli/pom.xml`  
`xinyi-rongli/src/main/`  
`xinyi-miniapp/`  

## 失败与边界

- 无DOM移植
- 不以H5成功等同微信通过
- 存量工程不覆盖

## 验证命令

```text
python docs/vehicle/V6.1/12_工程执行/scaffold.py --repo .
python docs/vehicle/V6.1/12_工程执行/scaffold.py --repo . --apply
npm --prefix xinyi-miniapp install
npm --prefix xinyi-miniapp run type-check
npm --prefix xinyi-miniapp run build:mp-weixin
python docs/vehicle/V6.1/12_工程执行/engineer.py run --repo . --kind backend-build
```

初次无小程序lockfile先npm install并提交生成结果，之后npm ci。部署/供应商凭据缺失不替代构建或资料证据。

## 完成判据

- 新模块与小程序实际编译；lockfile来自真实安装而非猜测
- 模板安装不覆盖框架/已有源码；没有数据库的纯构建不标业务通过
- 保存编译日志与独立复核，后续使用npm ci

## 持续执行

完成后更新真实任务账本和证据，继续taskctl next；不得把本任务完成写成M0整体完成。用户没有做M0，不要求用户先补M0。中止时保留恢复点，不清空工作区。

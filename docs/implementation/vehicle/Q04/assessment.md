# Q04 范围核对与当前实施口径

2026-11-09，起点1620dfc，分支rongli-wx-home。改动前41项单元基线已实际通过，证据baseline-unit.json/baseline-summary.txt。实现与最终测试以report.md及原始报告为准，本文件不替代验收。

## 用户最终确认

用户以微信官方bio-auth文档明确：仅需确认当前设备的使用者，暂不接腾讯云慧眼。当前采用微信SOTER本机生物认证，不再要求实名/活体核验。它验证的是设备已录入指纹或面容持有人，不能推断法律机主、姓名身份证匹配或实名活体。

驾驶证及背景核验由人工确认，无需凭证上传或标准发布；经办与复核两人分离，复核人可参与最终资格审核。公司权威归属沿A04档案；不默认主部门或客户端部门。禁止申请人本人自办/自审，历史关联及可信手机号识别到的后台身份在登记时冻结，换号不抹除限制。

Q01提交改为当前材料、同意、有效公司及四项可信结果：DEVICE_AUTH、BACKGROUND、LICENSE、TRAINING。培训保持Q03真实冻结课表/运行权威，最终资格批准与DRIVER授予仍属Q05。旧REALNAME/FACE只保留历史，不能改称设备认证。

## 工程边界

兼容旧face-session路由及vi_face_session物理表名，当前语义均为本机认证。随机挑战绑定本人申请、公司epoch、材料、真实MINI登录session/context及已发布WECHAT_APP配置；首次签名提交须在300秒技术窗口，原始JSON及签名不得trim或重序列化。服务端以原始OpenID调用微信verify_signature，客户端success/passed不作为证明。只在当前实际登录会话有效；完成后同会话切身份可用，新登录需重做。

GET纯DB读取。UNKNOWN恢复首次接受的加密原文，不重新替换签名；锁外网络验签，短事务租约/CAS与IO后实时鉴权/材料复查。人工确认和设备证明均从可信父事实发布认证结果，独立PASSED行或供应商名不满足门禁。共享权限、审计、outbox及幂等沿统一服务；失败关闭。

## 官方依据

- [微信生物认证说明](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/bio-auth.html)：SOTER启动与服务端验签。
- [startSoterAuthentication](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/soter/wx.startSoterAuthentication.html)：原始resultJSON、raw挑战及原始签名。
- [稳定版AccessToken](https://developers.weixin.qq.com/miniprogram/dev/server/API/mp-access-token/api_getstableaccesstoken.html)：服务端小程序凭据与稳定token，不在客户端保存AppSecret。

仅在受保护隔离库执行增量/结构对比；本轮不接腾讯慧眼，不调用真实微信或收费供应商，不部署生产。真实微信接口和真机原生SOTER另由L01/L02验收，回环签名器与GUI回调替身明确标注。

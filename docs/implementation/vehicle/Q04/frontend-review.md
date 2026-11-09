# Q04 前端定向复核结论

当前范围：微信 SOTER 本机使用者确认 + BACKGROUND/LICENSE 无凭证人工经办与独立复核 + TRAINING，当前四项不声明身份证实名、人证比对或活体。腾讯慧眼页面、配置调用、SDK与收费确认已删除。

本轮自查发现并修正：异步返回身份不符时，原代码仅丢弃响应但可能保留已加载的旧申请投影；现统一 requestCurrent/invalidateContext 清申请、挑战、同意勾选、未知意图及等待状态。服务端409读取冲突也锁写，须重读成功后解除。

原签名不 trim、不重新序列化或改写；本机适配只输出原 resultJSON/resultJSONSignature。服务端挑战与 authContent 原样传给实际 uni.startSoterAuthentication，先支持方式再逐项已录入检查；客户端不输出 passed。

未知写在本会话内冻结原目标/正文/UUID，新增恢复按钮在已有未知意图时也重试原意图；服务端已冻结未知签名且本地无旧请求时才使用 expectedVersion+recover:true。短期挑战/签名不进本地存储、路由或日志。当前登录/角色context变化会清旧意图。

PC候选申请来自真实API及其申请/公司/材料版本，不手填版本或公司；备注可空，不提交附件/标准字段。复核/封存按钮同时检查功能权限与服务端allowedActions。409保留填写后重读；unknown关弹窗、查看其他记录后仍原目标正文UUID，403清旧详情。后台查询只读安全metadata，不显示challenge/openid/签名/设备标识，不调用外部验证。

验证：PC真实页面+API夹具12项；H5真实页面+API夹具10项（含真实I04客户端API辅助切换上下文）；实际mp-weixin构建模块+隔离uni回调夹具5项。PC/mini浏览器errors/warnings/unknown=[]。mini类型检查0错误，mp/H5构建通过；PC生产构建通过，类型120=既有其它109+本次Vue声明衍生11，自身业务错误0。

边界：浏览器夹具不代替后端数据库权限/事务验证，不证明真实微信SOTER硬件、密码学或平台开通；MP适配夹具只校验真实构建模块调用协议。真实iOS/Android录入、取消及微信签名服务仍需真机与真实应用凭据验收。本记录为前端作者定向自查，独立安全复核由root对应审查代理负责。

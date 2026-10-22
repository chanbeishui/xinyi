# 隔离供应商模拟器

这是独立测试进程，不是业务服务器。仅模拟微信code/手机号凭据、路线、支付退款、活体和消息状态。全部控制请求需随机X-Sim-Secret，端口仅localhost，SQLite保存的是外部替身状态（不会承载vi_order等业务数据）。

运行：`VI_SIM_SECRET=<至少32位随机值> python simulator.py --host 127.0.0.1 --port 19091 --db /专用测试路径/provider.db`。通常由Compose与.env.vi.local启动。

调用顺序：POST /control/runs取得runId；POST /control/codes生成一次性凭据；真实后端的WeChatIdentityAdapter测试实现调用/wechat/exchange或/phone/exchange，照常建立真实账号/会话。/payments/prepay只PENDING，/control/payments/paid才改变外部事实并给出测试签名；真实后端查询或接受测试事件后更新自身业务。/refunds/create仅PROCESSING，不冒充到账。

消息ACCEPTED是模拟供应商受理，不是用户已读。活体最初PENDING；测试控制改变结果后后端查询得到结论。真实环境不装配模拟器，不接受“SIM_”凭据。

当前实现的失败：鉴权错误、code过期/重用、路线缺失、同单不同金额、支付/关单互斥、退款超额/重复、核验pending。网络丢包与任意请求延时通过测试代理或断开进程实现；不可声称当前模拟器已内建完整生产微信协议。

已提供19项局部自动测试。它们不证明Spring/MySQL或微信渠道已经联调。

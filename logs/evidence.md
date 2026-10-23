# 验收执行台账

生成时间：2026-10-23T16:23:42+08:00

## 一、自动化断言

共 327 条断言：通过 327，失败 0，跳过 0（通过率 100.0%）

| 断言 | 模块 | 级别 | 结果 | 断言项 | 说明 |
|---|---|---|---|---|---|
| ST-001 | 接口契约 | P0 | PASS | OK：两端接口路径与请求方法完全一致 |  |
| SS-001 | 数据访问 | P0 | PASS | OK：15 个 Mapper 的 SQL 与表结构一致 |  |
| SW-001 | 小程序构建 | P1 | PASS | 结论: OK |  |
| SG-001 | 侧栏与统计 | P1 | PASS | 侧栏存在 /rongli 目录 |  |
| SG-002 | 侧栏与统计 | P1 | PASS | 菜单 业务统计 指向 rongli/statistics/index |  |
| SG-003 | 侧栏与统计 | P1 | PASS | 菜单 支付渠道核对 指向 rongli/paymentReconcile/index |  |
| SG-004 | 侧栏与统计 | P1 | PASS | 统计接口可用  200 ['rangeDays', 'range', 'from', 'overview'] |  |
| SG-005 | 侧栏与统计 | P1 | PASS | 统计卡 4 张  样本服务订单=31 / 已支付金额=¥10230.00 / 已归还司机任务=0 / 待支付订单=0 |  |
| SG-006 | 侧栏与统计 | P1 | PASS | 近7日趋势 7 个点 |  |
| SG-007 | 侧栏与统计 | P1 | PASS | 指标口径 5 条 |  |
| SG-008 | 侧栏与统计 | P1 | PASS | 按站点概况有数据  1 个站点 |  |
| SG-009 | 侧栏与统计 | P1 | PASS | kpi 字段齐全  {'sampleOrders': 31, 'paidFen': 1023000, 'paidOrders': 31, 'refundedFen': 33000, 'netPaidFen': 990000, 'returnedAssignments': 0, 'pendingPayOrders': 0} |  |
| SG-010 | 侧栏与统计 | P1 | PASS | daily 旧字段保留 |  |
| SG-011 | 侧栏与统计 | P1 | PASS | range=WEEK 生效  rangeDays=30 |  |
| SG-012 | 侧栏与统计 | P1 | PASS | 旧调用 days=14 不回归  rangeDays=14 |  |
| SG-013 | 侧栏与统计 | P1 | PASS | 对账接口可用  200 ['pageNum', 'pageSize', 'total', 'rows'] |  |
| SG-014 | 侧栏与统计 | P1 | PASS | 待核实记录有数据  total=2 |  |
| SG-015 | 侧栏与统计 | P1 | PASS | 行 2 字段齐全  RL202610211022124707 RLPRL202610211022124707 关单与支付通知竞态 已支付/SUCCESS 33000 |  |
| SG-016 | 侧栏与统计 | P1 | PASS | 行 1 字段齐全  RL202610211021406441 RLPRL202610211021406441 关单与支付通知竞态 已支付/SUCCESS 33000 |  |
| SG-017 | 侧栏与统计 | P1 | PASS | 关键词按业务单号命中  RL202610211022124707 -> 1 |  |
| SG-018 | 侧栏与统计 | P1 | PASS | 关键词无匹配返回空  total=0 |  |
| SG-019 | 侧栏与统计 | P1 | PASS | 查询原交易可用  200 status=CHECKING |  |
| SG-020 | 侧栏与统计 | P1 | PASS | 核对退款登记结论可用  200 status=RESOLVED |  |
| SG-021 | 侧栏与统计 | P1 | PASS | 登记结论不覆盖问题类型  problemType=关单与支付通知竞态 conclusion=渠道确认为支付成功，按关闭订单退款处理 |  |
| SG-022 | 侧栏与统计 | P1 | PASS | 空结论被拒绝  400 |  |
| SP-001 | 后台页面 | P1 | PASS | 统计概览 /api/admin/statistics  http=200 daily=3 |  |
| SP-002 | 后台页面 | P1 | PASS | 导出任务列表 /api/admin/exports  http=200 total=0 |  |
| SP-003 | 后台页面 | P1 | PASS | 培训课程列表 /api/admin/training-courses  http=200 total=1 |  |
| SP-004 | 后台页面 | P1 | PASS | 课程题库详情（选项为 {code,text}）  http=200 questions=3 |  |
| SP-005 | 后台页面 | P1 | PASS | 处罚记录 /api/admin/penalties  http=200 total=0 |  |
| SP-006 | 后台页面 | P1 | PASS | 申诉列表 /api/admin/penalty-appeals  http=200 total=0 |  |
| SP-007 | 后台页面 | P1 | PASS | 通知模板 /api/admin/notification-templates  http=200 total=7 |  |
| SP-008 | 后台页面 | P1 | PASS | 补偿任务 /api/admin/jobs  http=200 total=6 |  |
| SP-009 | 后台页面 | P1 | PASS | 隐私案件 /api/admin/privacy-cases  http=200 total=0 |  |
| SP-010 | 后台页面 | P1 | PASS | 员工绑定申请接口已退役（V6.4 改手机号自动关联）  http=200 msg=No static resource api/admin/employee-binding-requests for request '/api/admin/employee-binding-requests'. |  |
| SP-011 | 后台页面 | P1 | PASS | 服务争议 /api/admin/disputes  http=200 total=0 |  |
| SP-012 | 后台页面 | P1 | PASS | 服务异常 /api/admin/exceptions  http=200 total=0 |  |
| SP-013 | 后台页面 | P1 | PASS | 结果更正 /api/admin/result-corrections  http=200 total=0 |  |
| SP-014 | 后台页面 | P1 | PASS | 支付对账异常 /api/admin/reconciliation  http=200 total=2 |  |
| SP-015 | 后台页面 | P1 | PASS | 小程序培训列表 /api/app/driver/qualification/training  http=200 rows=1 |  |
| SP-016 | 后台页面 | P1 | PASS | 小程序课程详情  http=200 questionCount=3 |  |
| SP-017 | 后台页面 | P1 | PASS | 小程序处罚列表 /api/app/driver/penalties  http=200 |  |
| SP-018 | 后台页面 | P1 | PASS | 小程序申诉列表 /api/app/driver/penalty-appeals  http=200 |  |
| SP-019 | 后台页面 | P1 | PASS | 小程序对账明细 /api/app/driver/reconciliation-entries  http=200 rows=3 |  |
| SP-020 | 后台页面 | P1 | PASS | 小程序收款记录 /api/app/driver/payment-records  http=200 |  |
| SK-001 | 存量接口 | P1 | PASS | 治理与导出列表 /api/admin/exports  rows=0 total=0 |  |
| SK-002 | 存量接口 | P1 | PASS | 隐私案件列表 /api/admin/privacy-cases  rows=0 total=0 |  |
| SK-003 | 存量接口 | P1 | PASS | 审计事件 /api/admin/audit-events  rows=10 total=2169 |  |
| SK-004 | 存量接口 | P1 | PASS | 业务统计 /api/admin/statistics  keys=['rangeDays', 'range', 'from', 'overview'] |  |
| SK-005 | 存量接口 | P1 | PASS | 支付渠道核对 /api/admin/reconciliation  rows=2 total=2 |  |
| SK-006 | 存量接口 | P1 | PASS | 争议列表 /api/admin/disputes  rows=0 total=0 |  |
| SK-007 | 存量接口 | P1 | PASS | 服务异常列表 /api/admin/exceptions  rows=0 total=0 |  |
| SK-008 | 存量接口 | P1 | PASS | 检测结果更正 /api/admin/result-corrections  rows=0 total=0 |  |
| SK-009 | 存量接口 | P1 | PASS | 通知模板 /api/admin/notification-templates  rows=7 total=7 |  |
| SK-010 | 存量接口 | P1 | PASS | 补偿任务 /api/admin/jobs  rows=6 total=6 |  |
| SK-011 | 存量接口 | P1 | PASS | 业务配置 /api/admin/config  keys=['effective', 'stored', 'paymentChannelLocked', 'hint'] |  |
| SK-012 | 存量接口 | P1 | PASS | 处罚列表 /api/admin/penalties  rows=0 total=0 |  |
| SK-013 | 存量接口 | P1 | PASS | 处罚申诉 /api/admin/penalty-appeals  rows=0 total=0 |  |
| SK-014 | 存量接口 | P1 | PASS | 培训课程 /api/admin/training-courses  rows=1 total=1 |  |
| SK-015 | 存量接口 | P1 | PASS | 收车待办 /api/admin/receiving/pending  rows=0 total=0 |  |
| SK-016 | 存量接口 | P1 | PASS | 检测结果待办 /api/admin/inspection/todos  rows=0 total=0 |  |
| SK-017 | 存量接口 | P1 | PASS | 派单邀约记录 /api/admin/dispatch/invites  rows=10 total=18 |  |
| SK-018 | 存量接口 | P1 | PASS | 工作台看板 /api/admin/dashboard  keys=['activeOrders', 'ordersToday', 'dispatching', 'manualDispatch'] |  |
| SK-019 | 存量接口 | P1 | PASS | 对账费用调整 /api/admin/driver-fee-adjustments  rows=0 total=0 |  |
| SK-020 | 存量接口 | P1 | PASS | 对账支付流水 /api/admin/driver-payment-records  rows=0 total=0 |  |
| SK-021 | 存量接口 | P1 | PASS | 对账单列表 /api/admin/driver-statements  rows=0 total=0 |  |
| SK-022 | 存量接口 | P1 | PASS | 检测点与容量 /api/admin/stations  rows=1 total=1 |  |
| SK-023 | 存量接口 | P1 | PASS | 计价规则 /api/admin/prices  rows=5 total=5 |  |
| SK-024 | 存量接口 | P1 | PASS | 代驾认证 /api/admin/driver-qualifications  rows=1 total=1 |  |
| SK-025 | 存量接口 | P1 | PASS | 代驾档案 /api/admin/drivers  rows=3 total=3 |  |
| SK-026 | 存量接口 | P1 | PASS | 订单列表 /api/admin/orders  rows=10 total=31 |  |
| SK-027 | 存量接口 | P1 | PASS | 车辆列表 /api/admin/vehicles  rows=10 total=44 |  |
| SK-028 | 存量接口 | P1 | PASS | 车主列表 /api/admin/owners  rows=4 total=4 |  |
| SR-001 | 本轮新增 | P0 | PASS | 派单配置下发半径阶梯 |  |
| SR-002 | 本轮新增 | P0 | PASS | 扩圈阶梯可通过管理端配置 |  |
| SR-003 | 本轮新增 | P0 | PASS | 可派单司机均已上报远端位置 |  |
| SR-004 | 本轮新增 | P0 | PASS | 立即派单接口可用 |  |
| SR-005 | 本轮新增 | P0 | PASS | 无候选时订单转入待人工安排 |  |
| SR-006 | 本轮新增 | P0 | PASS | 按阶梯扩圈 2 次（3 档半径） |  |
| SR-007 | 本轮新增 | P0 | PASS | 转人工原因为扩圈耗尽 |  |
| SR-008 | 本轮新增 | P0 | PASS | 扩圈事件记录轮次 2/3 |  |
| SR-009 | 本轮新增 | P0 | PASS | 司机 1 位置已置为取车点附近 |  |
| SR-010 | 本轮新增 | P0 | PASS | 阶梯可放宽到 200km |  |
| SR-011 | 本轮新增 | P0 | PASS | 第 2 轮命中后发起邀约 |  |
| SR-012 | 本轮新增 | P0 | PASS | 自动邀约携带扩圈轮次 2 |  |
| SR-013 | 本轮新增 | P0 | PASS | 邀约列表回显轮次 |  |
| SR-014 | 本轮新增 | P0 | PASS | 人工邀约可用 |  |
| SR-015 | 本轮新增 | P0 | PASS | 人工邀约写入当前轮次 |  |
| SR-016 | 本轮新增 | P0 | PASS | 订阅模板可通过管理端配置 |  |
| SR-017 | 本轮新增 | P0 | PASS | 邀约触达站内信 |  |
| SR-018 | 本轮新增 | P0 | PASS | 订阅事件已入 outbox 待投递 |  |
| SR-019 | 本轮新增 | P0 | PASS | 订阅事件被 outbox 消费完成 |  |
| SR-020 | 本轮新增 | P0 | PASS | 无订阅死信 |  |
| SR-021 | 本轮新增 | P0 | PASS | 站内信标记为已投订阅消息 |  |
| SR-022 | 本轮新增 | P0 | PASS | 无模板场景仍只发站内信 |  |
| SR-023 | 本轮新增 | P0 | PASS | 订阅总开关可关闭 |  |
| SR-024 | 本轮新增 | P0 | PASS | 代驾可见课程 |  |
| SR-025 | 本轮新增 | P0 | PASS | 开始学习会话成功（session_id 类型缺陷已修） |  |
| SR-026 | 本轮新增 | P0 | PASS | 首次心跳计入有效学时 |  |
| SR-027 | 本轮新增 | P0 | PASS | 重复序号不再累加（幂等） |  |
| SR-028 | 本轮新增 | P0 | PASS | 播放位置跳变被拒（防快进） |  |
| SR-029 | 本轮新增 | P0 | PASS | 伪造会话被拒 |  |
| SR-030 | 本轮新增 | P0 | PASS | 心跳超阈值时按阈值封顶 |  |
| SR-031 | 本轮新增 | P0 | PASS | 超时心跳留痕 |  |
| SR-032 | 本轮新增 | P0 | PASS | 过期会话被拒 |  |
| SR-033 | 本轮新增 | P0 | PASS | 部门测试账号可登录（非超管 + 全功能权限） |  |
| SR-034 | 本轮新增 | P0 | PASS | 订单按部门收敛（104 部门无订单） |  |
| SR-035 | 本轮新增 | P0 | PASS | 统计 KPI 按部门收敛 |  |
| SR-036 | 本轮新增 | P0 | PASS | 统计站点概况按部门收敛 |  |
| SR-037 | 本轮新增 | P0 | PASS | 工作台指标按部门收敛 |  |
| SR-038 | 本轮新增 | P0 | PASS | 司机主数据共享池保持可见（dept_id 为空不收敛） |  |
| SR-039 | 本轮新增 | P0 | PASS | 车辆主数据共享池保持可见 |  |
| STK-001 | 令牌续期 | P0 | PASS | 登录返回访问令牌与刷新令牌  sessionId=641 |  |
| STK-002 | 令牌续期 | P0 | PASS | 新签发的访问令牌可用  HTTP=200 |  |
| STK-003 | 令牌续期 | P0 | PASS | 切换角色成功  HTTP=200 |  |
| STK-004 | 令牌续期 | P0 | PASS | 切换角色返回了新令牌  ctx=2 |  |
| STK-005 | 令牌续期 | P0 | PASS | 切换后的旧访问令牌立即失效  HTTP=401 {'code': 'UNAUTHORIZED', 'message': '登录令牌已失效', 'msg': '登录令牌已失效'} |  |
| STK-006 | 令牌续期 | P0 | PASS | 切换后的新访问令牌可用（前端必须落盘）  HTTP=200 {'sessionId': '641', 'activeRole': 'OWNER', 'stationId': None, 'contextVersion': 2, 'absoluteExpiresAt': '2026-11-22T08:23:13.609Z', 'profile': {'accountId': '1', 'nickname': '演示车主', 'phoneBound': True, 'status': 'ACTIVE', 'roles': ['OWNER', 'DRIVER'], 'serviceName': '车主_001', 'driverQualificationStatus': 'APPROVED', 'driverListeningEnabled': True, 'employeeBound': False, 'employeeStationCount': 0, 'employeeBindSource': None}, 'stations': []} |  |
| STK-007 | 令牌续期 | P0 | PASS | 旧刷新令牌已被撤销，无法续期（不落盘就无法自愈）  HTTP=401 {'code': 'UNAUTHORIZED', 'message': '刷新令牌已失效，请重新进入小程序', 'msg': '刷新令牌已失效，请重新进入小程序'} |  |
| STK-008 | 令牌续期 | P0 | PASS | 新刷新令牌可正常续期  HTTP=200 |  |
| STK-009 | 令牌续期 | P0 | PASS | 并发刷新恰好一个成功、一个被拒  HTTP 分布=[200, 401] |  |
| STK-010 | 令牌续期 | P0 | PASS | 失败方给出明确的重复使用提示  {"code": "UNAUTHORIZED", "message": "刷新令牌已被使用，请重新进入小程序", "msg": "刷新令牌已被使用，请重新进入小程序"} |  |
| STK-011 | 令牌续期 | P0 | PASS | 并发中成功签发的令牌未被对方撤销  HTTP=200 {'sessionId': '641', 'activeRole': 'OWNER', 'stationId': None, 'contextVersion': 2, 'absoluteExpiresAt': '2026-11-22T08:23:13.609Z', 'profile': {'accountId': '1', 'nickname': '演示车主', 'phoneBound': True, 'status': 'ACTIVE', 'roles': ['OWNER', 'DRIVER'], 'serviceName': '车主_001', 'driverQualificationStatus': 'APPROVED', 'driverListeningEnabled': True, 'employeeBound': False, 'employeeStationCount': 0, 'employeeBindSource': None}, 'stations': []} |  |
| STK-012 | 令牌续期 | P0 | PASS | 刷新后上一代访问令牌按预期失效  HTTP=401 |  |
| STK-013 | 令牌续期 | P0 | PASS | 迟到 401 用当前令牌重放即可成功  stale=True replay=200 |  |
| STK-014 | 令牌续期 | P0 | PASS | 未发生多余轮换，刷新令牌仍然有效  HTTP=200 |  |
| SPH-001 | 手机号授权 | P0 | PASS | 后台管理员可登录（用于配置站点授权） |  |
| SPH-002 | 手机号授权 | P0 | PASS | 手机号授权登录成功 |  |
| SPH-003 | 手机号授权 | P0 | PASS | 登录返回手机号已绑定 |  |
| SPH-004 | 手机号授权 | P0 | PASS | 员工身份按手机号自动识别 |  |
| SPH-005 | 手机号授权 | P0 | PASS | 绑定来源标记为手机号 |  |
| SPH-006 | 手机号授权 | P0 | PASS | 未配置检测点时不产生检测员工视角 |  |
| SPH-007 | 手机号授权 | P0 | PASS | 新用户默认只有车主视角 |  |
| SPH-008 | 手机号授权 | P0 | PASS | openid 已写入员工绑定关系 |  |
| SPH-009 | 手机号授权 | P0 | PASS | 自动绑定标记为系统发起(bound_by IS NULL) |  |
| SPH-010 | 手机号授权 | P0 | PASS | 绑定来源为微信手机号唯一匹配 |  |
| SPH-011 | 手机号授权 | P0 | PASS | 自动绑定已固化手机号HMAC与授权版本快照 |  |
| SPH-012 | 手机号授权 | P0 | PASS | 手机号已加密落库 |  |
| SPH-013 | 手机号授权 | P0 | PASS | 无手机号登录仍可用（车主身份） |  |
| SPH-014 | 手机号授权 | P0 | PASS | 未授权手机号时无员工身份 |  |
| SPH-015 | 手机号授权 | P0 | PASS | 仅车主视角 |  |
| SPH-016 | 手机号授权 | P0 | PASS | 未通过代驾审核不能切换代驾视角 |  |
| SPH-017 | 手机号授权 | P0 | PASS | 手机号格式非法被拒绝 |  |
| SPH-018 | 手机号授权 | P0 | PASS | 后台可按手机号检索到员工 |  |
| SPH-019 | 手机号授权 | P0 | PASS | 后台新增站点绑定成功 |  |
| SPH-020 | 手机号授权 | P0 | PASS | 配置检测点后出现检测员工视角 |  |
| SPH-021 | 手机号授权 | P0 | PASS | 会话返回已授权站点 |  |
| SPH-022 | 手机号授权 | P0 | PASS | 可切换到检测员工视角 |  |
| SPH-023 | 手机号授权 | P0 | PASS | 检测员工可访问站点工单 |  |
| SPH-024 | 手机号授权 | P0 | PASS | 先以车主身份登录 |  |
| SPH-025 | 手机号授权 | P0 | PASS | 会话内授权手机号后自动绑定员工 |  |
| SPH-026 | 手机号授权 | P0 | PASS | 绑定变化后换发新令牌 |  |
| SPH-027 | 手机号授权 | P0 | PASS | 该员工无检测点授权仍是纯车主 |  |
| SPH-028 | 手机号授权 | P0 | PASS | 同一手机号新微信可登录 |  |
| SPH-029 | 手机号授权 | P0 | PASS | 同一员工仍只有一条有效绑定 |  |
| SPH-030 | 手机号授权 | P0 | PASS | 原微信账号仍持有绑定（未被新账号抢占） |  |
| SPH-031 | 手机号授权 | P0 | PASS | 新微信账号不获得员工身份 |  |
| SPH-032 | 手机号授权 | P0 | PASS | 冲突已留审计（EMPLOYEE_LINK_CONFLICT） |  |
| SPH-033 | 手机号授权 | P0 | PASS | 短信绑定手机号成功 |  |
| SPH-034 | 手机号授权 | P0 | PASS | 短信绑定不产生员工关联 |  |
| SDF-001 | 履约全链路 | P0 | PASS | 200 车主登录 |  |
| SDF-002 | 履约全链路 | P0 | PASS | 200 代驾账号登录（未申请） |  |
| SDF-003 | 履约全链路 | P0 | PASS | 200 查询代驾资格档案 |  |
| SDF-004 | 履约全链路 | P0 | PASS | --- 未审核不能切换代驾视角  403 {"code": "FORBIDDEN", "message": "当前账户未开通该视角", "msg": "当前账户未开通该视角"} |  |
| SDF-005 | 履约全链路 | P0 | PASS | 200 听单开关 |  |
| SDF-006 | 履约全链路 | P0 | PASS | 200 上报位置 |  |
| SDF-007 | 履约全链路 | P0 | PASS | 200 切换代驾视角 |  |
| SDF-008 | 履约全链路 | P0 | PASS | 200 可选时段 |  |
| SDF-009 | 履约全链路 | P0 | PASS | 200 车辆列表 |  |
| SDF-010 | 履约全链路 | P0 | PASS | 200 新增车辆 |  |
| SDF-011 | 履约全链路 | P0 | PASS | 200 地址列表 |  |
| SDF-012 | 履约全链路 | P0 | PASS | 200 新增地址 |  |
| SDF-013 | 履约全链路 | P0 | PASS | 200 试算报价 |  |
| SDF-014 | 履约全链路 | P0 | PASS | 200 提交预约单 |  |
| SDF-015 | 履约全链路 | P0 | PASS | 200 预支付 |  |
| SDF-016 | 履约全链路 | P0 | PASS | 200 模拟支付 |  |
| SDF-017 | 履约全链路 | P0 | PASS | 200 接受邀约 |  |
| SDF-018 | 履约全链路 | P0 | PASS | 200 当前承接 |  |
| SDF-019 | 履约全链路 | P0 | PASS | 200 出发取车 |  |
| SDF-020 | 履约全链路 | P0 | PASS | 200 到达取车点 |  |
| SDF-021 | 履约全链路 | P0 | PASS | 200 创建交车交接 |  |
| SDF-022 | 履约全链路 | P0 | PASS | 200 获取交接码（车主侧） |  |
| SDF-023 | 履约全链路 | P0 | PASS | 200 确认交接 |  |
| SDF-024 | 履约全链路 | P0 | PASS | 200 司机到达检测站 |  |
| SDF-025 | 履约全链路 | P0 | PASS | 200 站点工单列表 |  |
| SDF-026 | 履约全链路 | P0 | PASS | 200 检测点发起收车交接 |  |
| SDF-027 | 履约全链路 | P0 | PASS | 200 检测点确认收车（核验方） |  |
| SDF-028 | 履约全链路 | P0 | PASS | --- 核验方确认后为待对方确认  status=PARTIAL |  |
| SDF-029 | 履约全链路 | P0 | PASS | 200 司机确认已交付（持码方） |  |
| SDF-030 | 履约全链路 | P0 | PASS | --- 双方确认后交接完成  status=CONFIRMED |  |
| SDF-031 | 履约全链路 | P0 | PASS | 200 工单详情 |  |
| SDF-032 | 履约全链路 | P0 | PASS | 200 核对并保存行驶证 |  |
| SDF-033 | 履约全链路 | P0 | PASS | 200 登记检测结果 |  |
| SDF-034 | 履约全链路 | P0 | PASS | 200 发送取车通知 |  |
| SDF-035 | 履约全链路 | P0 | PASS | 200 站点发起交车给司机 |  |
| SDF-036 | 履约全链路 | P0 | PASS | 200 司机确认取车 |  |
| SDF-037 | 履约全链路 | P0 | PASS | 200 司机发起还车交接 |  |
| SDF-038 | 履约全链路 | P0 | PASS | 200 车主确认收车 |  |
| SDF-039 | 履约全链路 | P0 | PASS | 200 车主确认服务 |  |
| SDF-040 | 履约全链路 | P0 | PASS | 200 订单进度（车主侧） |  |
| SAF-001 | 售后申诉对账 | P0 | PASS | 200 车主登录 |  |
| SAF-002 | 售后申诉对账 | P0 | PASS | 200 可选时段 |  |
| SAF-003 | 售后申诉对账 | P0 | PASS | 200 车辆列表 |  |
| SAF-004 | 售后申诉对账 | P0 | PASS | 200 新增车辆 |  |
| SAF-005 | 售后申诉对账 | P0 | PASS | 200 地址列表 |  |
| SAF-006 | 售后申诉对账 | P0 | PASS | 200 新增地址 |  |
| SAF-007 | 售后申诉对账 | P0 | PASS | 200 试算报价 |  |
| SAF-008 | 售后申诉对账 | P0 | PASS | 200 提交预约单 |  |
| SAF-009 | 售后申诉对账 | P0 | PASS | 200 预支付 |  |
| SAF-010 | 售后申诉对账 | P0 | PASS | 200 模拟支付 |  |
| SAF-011 | 售后申诉对账 | P0 | PASS | 200 车主发起售后（取消退款） |  |
| SAF-012 | 售后申诉对账 | P0 | PASS | 200 后台售后列表 |  |
| SAF-013 | 售后申诉对账 | P0 | PASS | --- 新售后单出现在后台列表 |  |
| SAF-014 | 售后申诉对账 | P0 | PASS | 200 审核预览（可退金额） |  |
| SAF-015 | 售后申诉对账 | P0 | PASS | 200 审核通过（无需实还，直接退款） |  |
| SAF-016 | 售后申诉对账 | P0 | PASS | --- 售后单已结案  caseStatus=CLOSED |  |
| SAF-017 | 售后申诉对账 | P0 | PASS | --- 订单已取消  orderStatus=CANCELLED |  |
| SAF-018 | 售后申诉对账 | P0 | PASS | --- 退款已完成  refundStatus=SUCCESS |  |
| SAF-019 | 售后申诉对账 | P0 | PASS | --- 退款金额与申请一致  33000 vs 33000 |  |
| SAF-020 | 售后申诉对账 | P0 | PASS | 200 退款单详情 |  |
| SAF-021 | 售后申诉对账 | P0 | PASS | --- 退款单状态为成功  status=SUCCESS |  |
| SAF-022 | 售后申诉对账 | P0 | PASS | 200 主动查退款状态 |  |
| SAF-023 | 售后申诉对账 | P0 | PASS | 200 代驾账号登录 |  |
| SAF-024 | 售后申诉对账 | P0 | PASS | 200 代驾资格档案 |  |
| SAF-025 | 售后申诉对账 | P0 | PASS | 200 切换代驾视角 |  |
| SAF-026 | 售后申诉对账 | P0 | PASS | --- 罚款记录已生成  penaltyId=23 |  |
| SAF-027 | 售后申诉对账 | P0 | PASS | 200 代驾查看罚款列表 |  |
| SAF-028 | 售后申诉对账 | P0 | PASS | --- 罚款出现在代驾端列表 |  |
| SAF-029 | 售后申诉对账 | P0 | PASS | 200 提交申诉 |  |
| SAF-030 | 售后申诉对账 | P0 | PASS | --- 申诉已受理  appealId=23 |  |
| SAF-031 | 售后申诉对账 | P0 | PASS | 200 后台申诉列表 |  |
| SAF-032 | 售后申诉对账 | P0 | PASS | 200 审核申诉（通过） |  |
| SAF-033 | 售后申诉对账 | P0 | PASS | --- 处罚已撤销  status=REVOKED |  |
| SAF-034 | 售后申诉对账 | P0 | PASS | --- 对账异常单已创建  caseId=27 |  |
| SAF-035 | 售后申诉对账 | P0 | PASS | 200 对账异常列表 |  |
| SAF-036 | 售后申诉对账 | P0 | PASS | --- 异常单出现在对账列表 |  |
| SAF-037 | 售后申诉对账 | P0 | PASS | 200 发起渠道重查 |  |
| SAF-038 | 售后申诉对账 | P0 | PASS | 200 登记对账结论 |  |
| SAF-039 | 售后申诉对账 | P0 | PASS | --- 对账单已关闭  status=RESOLVED |  |
| SCF-001 | 配置页校验 | P0 | PASS | --- 微信支付配置可读  status=MOCK ready=False |  |
| SCF-002 | 配置页校验 | P0 | PASS | --- 短信通道配置可读  status=MOCK ready=False |  |
| SCF-003 | 配置页校验 | P0 | PASS | --- 订阅消息配置可读  status=INCOMPLETE ready=False |  |
| SCF-004 | 配置页校验 | P0 | PASS | --- 切真实通道但未填凭据被拒绝  HTTP 400 |  |
| SCF-005 | 配置页校验 | P0 | PASS | --- 缺失项逐条列出（至少 7 项）  微信支付配置不完整：小程序 AppID（wx 开头 18 位）；商户号（8~12 位数字）；APIv3 密钥（32 位字母数字）；商户证书序列号（十六进制）；商户私钥；支付回调地址（https）；微信支付平台证书（已开启回调验签，必须提供） |  |
| SCF-006 | 配置页校验 | P0 | PASS | --- 被拒后通道未被改动  channel=MOCK |  |
| SCF-007 | 配置页校验 | P0 | PASS | --- AppID 格式错误被拒绝  HTTP 400 小程序 AppID 格式不正确（wx 开头 18 位） |  |
| SCF-008 | 配置页校验 | P0 | PASS | --- 商户号格式错误被拒绝  HTTP 400 商户号格式不正确（8~12 位数字） |  |
| SCF-009 | 配置页校验 | P0 | PASS | --- APIv3 密钥长度不符被拒绝  HTTP 400 APIv3 密钥必须是 32 位字母数字 |  |
| SCF-010 | 配置页校验 | P0 | PASS | --- 联调通道可保存非凭据字段  HTTP 200 已保存并即时生效 |  |
| SCF-011 | 配置页校验 | P0 | PASS | --- 重新读取即为新值（缓存已失效）  description=配置页验收用描述 |  |
| SCF-012 | 配置页校验 | P0 | PASS | --- 开启但未填凭据被拒绝  HTTP 400 |  |
| SCF-013 | 配置页校验 | P0 | PASS | --- 缺失项点名到具体字段  短信配置不完整：短信签名（2~20 字符）；短信模板编号；阿里云 AccessKeyId；阿里云 AccessKeySecret |  |
| SCF-014 | 配置页校验 | P0 | PASS | --- 被拒后开关未被打开  enabled=False |  |
| SCF-015 | 配置页校验 | P0 | PASS | --- 重发间隔过小被拒绝  HTTP 400 重发间隔需在 10~600 秒之间 |  |
| SCF-016 | 配置页校验 | P0 | PASS | --- 重发间隔过大被拒绝  HTTP 400 重发间隔需在 10~600 秒之间 |  |
| SCF-017 | 配置页校验 | P0 | PASS | --- 日上限过小被拒绝  HTTP 400 单号日上限需在 1~50 条之间 |  |
| SCF-018 | 配置页校验 | P0 | PASS | --- 日上限过大被拒绝  HTTP 400 单号日上限需在 1~50 条之间 |  |
| SCF-019 | 配置页校验 | P0 | PASS | --- 模板 ID 格式错误被拒绝  HTTP 400 订阅消息配置有误：派单邀约 模板 ID 格式不正确（字母数字下划线连字符，10~64 位） |  |
| SCF-020 | 配置页校验 | P0 | PASS | --- 运行环境非法值被拒绝  HTTP 400 订阅消息配置有误：运行环境只能是 developer / trial / formal |  |
| SCF-021 | 配置页校验 | P0 | PASS | --- 字段映射键名非法被拒绝  HTTP 400 订阅消息配置有误：字段名 Thing 不合法，应形如 thing1 / time2 |  |
| SCF-022 | 配置页校验 | P0 | PASS | --- 关闭订阅可以保存（不被必填项卡死）  HTTP 200 已保存，订阅消息已关闭，仅发站内信 |  |
| SCF-023 | 配置页校验 | P0 | PASS | --- 再次开启也能保存（不锁死）  HTTP 200 已保存。还差：至少配置一个场景的模板 ID；小程序 AppID（订阅消息与登录共用）；小程序 AppSecret（订阅消息与登录共用） |  |
| SCF-024 | 配置页校验 | P0 | PASS | --- 缺模板时状态为 INCOMPLETE 且有提示  status=INCOMPLETE message=已保存。还差：至少配置一个场景的模板 ID；小程序 AppID（订阅消息与登录共用）；小程序 AppSecret（订阅消息与登录共用） |  |
| SCF-025 | 配置页校验 | P0 | PASS | --- 微信支付已还原  HTTP 200 |  |
| SCF-026 | 配置页校验 | P0 | PASS | --- 短信通道已还原  HTTP 200 |  |
| SCF-027 | 配置页校验 | P0 | PASS | --- 订阅消息已还原  HTTP 200 |  |
| STM-001 | 时间口径 | P0 | PASS | 后台登录  → HTTP 200 |  |
| STM-002 | 时间口径 | P0 | PASS | 派单队列可读  → 共 3 条 |  |
| STM-003 | 时间口径 | P0 | PASS | 派单队列 时间形状统一（无裸 LocalDateTime）  → 全部 ISO8601 UTC |  |
| STM-004 | 时间口径 | P0 | PASS | 派单队列有上门时段文案  → pickupWindowLabel='2026-10-21 13:00 - 15:00' |  |
| STM-005 | 时间口径 | P0 | PASS | 时段文案起点与 pickupStartsAt 一致  → 2026-10-21 13:00 - 15:00 起点应为 2026-10-21 13:00 |  |
| STM-006 | 时间口径 | P0 | PASS | 派单时间为北京时间文本  → 接口 2026-10-21T04:30:00.000Z → 展示 2026-10-21 12:30:00 |  |
| STM-007 | 时间口径 | P0 | PASS | 订单列表 时间形状统一（无裸 LocalDateTime）  → 全部 ISO8601 UTC |  |
| STM-008 | 时间口径 | P0 | PASS | 订单列表有时间字段  → 5 条含 pickupStartsAt |  |
| STM-009 | 时间口径 | P0 | PASS | 订单列表有上门时段文案  → 示例 '2026-10-24 15:00 - 17:00' |  |
| STM-010 | 时间口径 | P0 | PASS | 车主列表可读  → HTTP 200 |  |
| STM-011 | 时间口径 | P0 | PASS | 车主列表 时间形状统一（无裸 LocalDateTime）  → 全部 ISO8601 UTC |  |
| STM-012 | 时间口径 | P0 | PASS | 车主列表 字段为驼峰  → 无下划线键 |  |
| STM-013 | 时间口径 | P0 | PASS | 车辆列表可读  → HTTP 200 |  |
| STM-014 | 时间口径 | P0 | PASS | 车辆列表 时间形状统一（无裸 LocalDateTime）  → 全部 ISO8601 UTC |  |
| STM-015 | 时间口径 | P0 | PASS | 车辆列表 字段为驼峰  → 无下划线键 |  |
| STM-016 | 时间口径 | P0 | PASS | 车主订单可读  → HTTP 200 |  |
| STM-017 | 时间口径 | P0 | PASS | 车主订单 时间形状统一（无裸 LocalDateTime）  → 全部 ISO8601 UTC |  |
| STM-018 | 时间口径 | P0 | PASS | 车主订单 字段为驼峰  → 无下划线键 |  |
| STM-019 | 时间口径 | P0 | PASS | 审计事件可读  → HTTP 200 |  |
| STM-020 | 时间口径 | P0 | PASS | 审计事件 时间形状统一（无裸 LocalDateTime）  → 全部 ISO8601 UTC |  |
| STM-021 | 时间口径 | P0 | PASS | 审计事件 字段为驼峰  → 无下划线键 |  |
| STM-022 | 时间口径 | P0 | PASS | 订单创建时间与单号内嵌时间一致  → 抽查 5 条全部一致（+08 换算后） |  |
| STM-023 | 时间口径 | P0 | PASS | UTC 05:00 应展示为北京时间 13:00  → 2026-10-21T05:00:00.000Z → 2026-10-21 13:00:00 |  |
| STM-024 | 时间口径 | P0 | PASS | 裸 LocalDateTime 按 UTC 解释  → 2026-10-21T02:18:02.638 → 2026-10-21 10:18:02 |  |
| SIC-001 | 第三方配置 | P0 | PASS | S1 十二张新增表全部存在  — count=12 |  |
| SIC-002 | 第三方配置 | P0 | PASS | S2 vi_payment_order 新增 9 列齐全  — count=9 |  |
| SIC-003 | 第三方配置 | P0 | PASS | S3 vi_refund_order 新增 2 列齐全  — count=2 |  |
| SIC-004 | 第三方配置 | P0 | PASS | S4 菜单 2240-2258 共 19 条  — count=19 |  |
| SIC-005 | 第三方配置 | P0 | PASS | S5 八个页面组件文件都存在（否则打开即 404）  — pages=8 missing=[] |  |
| SIC-006 | 第三方配置 | P0 | PASS | S6 五个业务参数无重复（迁移可重复执行）  — rows=5 distinct=5 |  |
| SIC-007 | 第三方配置 | P0 | PASS | S6b 五个参数键名与设计一致且各一条  — rongli.integration.environment,rongli.integration.callbackBaseUrl,rongli.integration.checkTtlMinutes,rongli.integration.reauthWindowMinutes,rongli.integration.masterKeyVersion |  |
| SIC-008 | 第三方配置 | P0 | PASS | S6c 成员变更表版本约束已建立且不依赖不存在的 version 列  — clause=((`revision_before` >= 0) and (`revision_after` >= 0)) version_col=0 |  |
| SIC-009 | 第三方配置 | P0 | PASS | S7 资料包 79 张表在本库都能找到  — expected=79 current=80 lacking=[] |  |
| SIC-010 | 第三方配置 | P0 | PASS | S8 多出的表都已在迁移脚本里声明（退役表/本地扩展）  — extra=['vi_sms_send_log'] |  |
| SIC-011 | 第三方配置 | P0 | PASS | 后台登录  — admin |  |
| SIC-012 | 第三方配置 | P0 | PASS | R1 35 个新增端点都已注册（不是被静态资源兜底吞掉）  — not-ok=0 [] |  |
| SIC-013 | 第三方配置 | P0 | PASS | R2 配置档案列表可读  — http=200 total=20 |  |
| SIC-014 | 第三方配置 | P0 | PASS | R3 未登录访问被拒（业务码 401/403，不是静默放行）  — http=200 bodyCode=401 msg=请求访问：/api/admin/integrations/profiles，认证失败，无法访问系统资源 |  |
| SIC-015 | 第三方配置 | P0 | PASS | R4 退役的后台绑定接口确实下线  — 仍可访问=[] |  |
| SIC-016 | 第三方配置 | P0 | PASS | R5 小程序侧绑定声明/兑换端点已从控制器移除  — 残留=[]（鉴权拦截器先于路由，HTTP 层无法判定） |  |
| SIC-017 | 第三方配置 | P0 | PASS | L1 TEST 环境支付策略档案已存在（复用）  — profileId=1 |  |
| SIC-018 | 第三方配置 | P0 | PASS | L2 保存双关草稿  — revision=1 hash=c1ddf8ee9231 version=39 |  |
| SIC-019 | 第三方配置 | P0 | PASS | L3 未校验不允许发布  — http=422 code=VI_CONFIG_CHECK_REQUIRED msg=发布前必须先执行校验 |  |
| SIC-020 | 第三方配置 | P0 | PASS | L4 本地校验通过（双关本身是合法状态）  — http=200 status=PASS issues=2 |  |
| SIC-021 | 第三方配置 | P0 | PASS | L5 双关发布必须显式确认（不是免费模式）  — http=422 code=VI_CONFIG_INVALID msg=两种支付方式都关闭会暂停新付费预约，请勾选确认后再发布 |  |
| SIC-022 | 第三方配置 | P0 | PASS | L6 确认后发布成功且生效指针指向该版本  — http=200 active=1 revision=1 |  |
| SIC-023 | 第三方配置 | P0 | PASS | L7 发布产生审计记录（含原因与操作人）  — http=200 rows=20 |  |
| SIC-024 | 第三方配置 | P0 | PASS | L8 缺凭据不能发布微信支付（校验判 FAIL 且明确指出缺哪个凭据）  — check=FAIL issues=9 |  |
| SIC-025 | 第三方配置 | P0 | PASS | N1 核实人不得为登记人（服务端 403，前端不放行）  — IntegrationPaymentService.verifyProof |  |
| SIC-026 | 第三方配置 | P0 | PASS | N2 线下收款只接受一次全额，少款/多款拒绝  — IntegrationPaymentService.registerProof |  |
| SIC-027 | 第三方配置 | P0 | PASS | N3 只有 FAILED 才允许重试，UNKNOWN 只查回执  — IntegrationMessageService.queryReceipt/retryDelivery |  |
| SND-001 | 消息自动下发 | P0 | PASS | 验收账号可登录 | 200 |
| SND-002 | 消息自动下发 | P0 | PASS | 验收账号已绑定手机号 | ['1'] |
| SND-003 | 消息自动下发 | P0 | PASS | 下单并支付成功 | RL202610231623418157 |
| SND-004 | 消息自动下发 | P0 | PASS | 支付成功触发了第三方通道投递记录 | ['SKIPPED\tTENCENT_SMS\tBOOKING_CONFIRMED\tVI_NOTIF_NO_PROFILE\t15'] |
| SND-005 | 消息自动下发 | P0 | PASS | 投递走了配置的短信通道 | ['SKIPPED', 'TENCENT_SMS', 'BOOKING_CONFIRMED', 'VI_NOTIF_NO_PROFILE', '15'] |
| SND-006 | 消息自动下发 | P0 | PASS | 通道未生效时判 SKIPPED 而非静默丢弃或 FAILED | ['SKIPPED', 'TENCENT_SMS', 'BOOKING_CONFIRMED', 'VI_NOTIF_NO_PROFILE', '15'] |
| SND-007 | 消息自动下发 | P0 | PASS | 跳过原因可读（通道档案未生效） | ['SKIPPED', 'TENCENT_SMS', 'BOOKING_CONFIRMED', 'VI_NOTIF_NO_PROFILE', '15'] |
| SND-008 | 消息自动下发 | P0 | PASS | 投递记录回指到配置的那条模板 | ['SKIPPED', 'TENCENT_SMS', 'BOOKING_CONFIRMED', 'VI_NOTIF_NO_PROFILE', '15'] |
| SND-009 | 消息自动下发 | P0 | PASS | 通知未发出不影响业务：订单照常推进 | SCHEDULED |
| SND-010 | 消息自动下发 | P0 | PASS | 同一事件不会产生第二条投递 | before=1 after=1 |

## 二、资料包验收规格（298 条）执行现状

来源：`docs/acceptance/test_cases_V6.0.json`，共 298 条。

> 本节状态取自规格文件本身（`status` 字段），执行器**不代替人工判定**。
> 自动化断言只覆盖其中一部分实现不变量，不能据此宣称对应业务用例通过。

按状态：NOT_RUN 298

| 模块 | 总数 | P0 | P1 | 已标记完成 |
|---|---|---|---|---|
| 勾选组单与未结清 | 40 | 40 | 0 | 0 |
| 对账内核价 | 30 | 30 | 0 | 0 |
| 交接归还 | 23 | 23 | 0 | 0 |
| 身份权限 | 18 | 18 | 0 | 0 |
| 预约计价 | 18 | 0 | 18 | 0 |
| 派单资格 | 16 | 0 | 16 | 0 |
| 并发补偿 | 16 | 16 | 0 | 0 |
| 取消重派 | 15 | 15 | 0 | 0 |
| 支付关单 | 14 | 14 | 0 | 0 |
| 退款资金 | 12 | 12 | 0 | 0 |
| 统一售后 | 12 | 12 | 0 | 0 |
| 代驾对账 | 12 | 12 | 0 | 0 |
| 检测结果 | 10 | 0 | 10 | 0 |
| 资料列表 | 8 | 8 | 0 | 0 |
| 代驾费用 | 8 | 8 | 0 | 0 |
| 付款核对 | 7 | 7 | 0 | 0 |
| 字段投影 | 6 | 6 | 0 | 0 |
| 交互 | 5 | 0 | 5 | 0 |
| 数据库 | 4 | 4 | 0 | 0 |
| 资料CAS | 3 | 0 | 3 | 0 |
| 培训 | 3 | 0 | 3 | 0 |
| 安全 | 2 | 2 | 0 | 0 |
| 会话 | 2 | 2 | 0 | 0 |
| 错误契约 | 2 | 0 | 2 | 0 |
| 时间 | 2 | 0 | 2 | 0 |
| 组单 | 2 | 0 | 2 | 0 |
| 迁移 | 1 | 1 | 0 | 0 |
| 路由 | 1 | 0 | 1 | 0 |
| 权限 | 1 | 0 | 1 | 0 |
| 绑定 | 1 | 0 | 1 | 0 |
| 处罚 | 1 | 0 | 1 | 0 |
| 活体 | 1 | 0 | 1 | 0 |
| 并发 | 1 | 1 | 0 | 0 |
| 初始化 | 1 | 0 | 1 | 0 |

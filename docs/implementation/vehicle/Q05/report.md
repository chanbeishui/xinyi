# Q05 资格审批、生效快照与到期复审

已接通后台列表、详情和审核三个真实接口，落实当前四项可信结果的最终资格审批。基线02b5329，分支rongli-wx-home，日期2026-11-10。用户此前确认的SOTER本机认证、无凭证双人人工及培训范围保持；SOTER不证明实名活体，人工经办/复核人员可以参与最终审核，申请人本人不能自审。

## 审核与生效行为

后台使用独立ADMIN投影，分开展示本次冻结提交与当前生效资格；普通详情不返回全身份证、原图URL、内部意见或密文。三接口为GET /api/admin/driver-qualifications、GET /api/admin/driver-qualifications/{applicationId}、POST同路径/audit。主源当前305操作、421 schema、91表、20生成列。

审核精确九键：expectedVersion、expectedProfileVersion、expectedSubmissionRevision、expectedCompanyRevision、decision、opinion、publicOpinion、rejectFields、reviewDueAt。四版本分别绑定申请、档案、实际提交和公司epoch，UUID幂等。APPROVE必须显式未来UTC截止、无补正字段；REJECT/REPAIR_REJECTION必须非空公开意见和精确可改字段，reviewDueAt为null。内部意见按实际reviewId独立AAD加密，不向GET、本人、幂等结果或普通审计明文输出。

批准只消费认证封装的不可变提交：公司epoch、实际提交MINI会话、已知后台同人、材料/四证件、四proof及本人同意均冻结。退出或另次登录不会废除已提交事实，不要求审核员伪装成MINI；草稿/补正及新提交仍严格要求当前实际登录会话的设备认证。提交时点四项验源核对原始父事实和核验时间，不能用后改草稿、客户端passed、任意供应商名或独立PASSED行替代。新同意/课程正常发布不覆盖已冻结事实。

APPROVE同事务追加审核历史、真实生效版本和认证封装，再更新档案证件/复审/快照指针及申请。首次批准才出现DRIVER身份，仍保留OWNER，用户主动切换；不自动开行政新邀约、听单或复制资料/任务。复审驳回保留旧生效资格、证件、行政/听单和车辆责任。旧REJECTED无补正结构可显式修复，只有公开说明与精确补正字段，不批准、不开放缺可信提交的原图；修复后重新提交仍过完整门禁。

同申请补正重提会形成新的提交修订；当前工作态旧公开意见和rejectFields清除，不可变审核历史原样保留。跨公司重提后，每条历史review另按其原公司query范围过滤，不把旧公司的审核人或意见交给新公司；原公司无权当前路由时404，不通过新历史API扩大访问。

## 数据归属、权限与会话

列表/详情按当前提交的原公司读取，草稿或缺归属历史仅继承明确父档案公司用于诊断/修复。当前档案公司字段、版本及生效投影另需当前公司query，原公司历史读权不能泄露新公司的资料。审核要求query/audit分别覆盖冻结原公司与当前档案公司并集；批准还须当前公司/专用epoch与提交相同，A转B再转A也不能复活旧提交。普通备注导致的profile CAS变化会409重读，不等同材料证明失效。空范围失败关闭，批量任职不能用主部门替代。

禁止已知同一申请人自审：冻结后台身份、可信员工历史关联和已验证手机号关联共同校验，不能比较ADMIN userId与MINI accountId数值。人工经办人和复核人不会被额外排除最终审核。权限、锁序、写入、幂等、审计、能力版本及outbox同事务，失败整体回滚，重放仍重新鉴权。

ADMIN每次检查真实登录与数据库auth_version，旧Token即使Redis删除失败也拒绝；没有修改后台用户角色/任职或直接写其授权表。MINI审核与到期递增vi_account_security.permission_version，保留session_epoch，不注销设备、不自动切换视角。曾批准的DRIVER是历史/在手业务入口，不是实时准驾或新单许可。

原effectiveQualificationStatus按既有契约保留生效档案持久审核状态；本人实时状态以session.driverQualification为先，后台详情以effectiveQualification.effectiveStatus判断，列表标题明确“生效档案状态”。不能凭raw APPROVED或DRIVER直接承接新单。

## 截止与到期任务

审核员每次明确未来reviewDueAt，UTC Z、四位年份、秒小数不超过三位；超范围/精度400。没有自动一年或提前30天默认值。证件按Asia/Shanghai日历日含到期当日，实际expiresAt为复审UTC截止与证件到期日次日00:00的较早者；明确长期证件仍需复审截止。

批准事务通过原生outbox登记QUALIFICATION_EXPIRY:{snapshotId}永久事件，绑定司机、快照及资格版本。重启扫描只补当前可信版本缺失的任务，不覆写已完成/失败任务；领取/租约/重试沿统一relay，旧版本任务不能使新资格过期。定时执行只把当前版本资格改EXPIRED，同事务能力版本、审计和最小事件；不改旧批准申请、行政/听单、承接/占用或车辆保管。

实时新单基础Policy检查实际资格、公司epoch、上海证件日期和UTC复审，不依赖Job准时。无新快照的存量保留既有判据，不把NULL指针/旧数据说成新封装已验真；Policy不宣称每次重验所有历史snapshot seal。D01/D02仍须在实际听单、派单和接受入口复用并补齐定位、准驾匹配等最终条件，本任务不冒称已有完整自动/人工派单。

vi.jobs.qualification-enabled默认false；测试显式启用并用同一viDeadlineClock推进真实Job/outbox。部署启用须配置该开关及扫描间隔vi.jobs.qualification-delay-ms（技术默认60000毫秒）；关闭或延迟扫描不放行过期新单。提前运营提醒无正式政策，本轮不自动发送，N01另接通知。

## 受控查看证件

原图来自既有Q01四证件资料，不是给人工核验新增凭证上传门槛。ADMIN只能读已认证当前提交的四个确切文件：资格query、sensitive和文件query/access分别按原公司授权，校验本人上传/父申请/用途和READY；草稿、无seal历史、同公司其他文件均不开放。无后台证件写权限。

F01原图READ票沿现有60秒，固定同源私有传输、真实后台登录头；票据MAC增加submissionId/revision/seal摘要，复用同一四图重新提交也不复活旧票。目的理由绑定文件、操作者和requestId认证加密审计，普通日志/GET无理由明文。IO前后重新鉴权，文件完整性验证与实际读取审计保留；PC临时blob关闭/换申请及时释放，不公开映射持久卷。

## SQL与升级

已有环境先执行至 **sql/migration_20261109_03_device_authentication.sql**，再执行本次 **sql/migration_20261110_qualification_review.sql**。同步修改完整初始化 **sql/ry_20260417.sql**、**docs/vehicle/V6.4/05_数据设计/001_vi业务表_初始化_V6.4.sql**；新环境直接最新完整初始化，不叠加同一增量。

2改表：vi_driver_profile、vi_driver_application_submission；2新表：vi_qualification_review、vi_qualification_snapshot。稳定菜单2971400..2971403四条，路径/driver-management/qualification，组件rongli/qualification/index。仅建入口，不自动授权角色；通过统一授权服务分配list/query/audit/sensitive及各自公司范围，原图另需文件两权限。

执行前备份两张旧表、sys_menu和SHOW CREATE TABLE，暂停资格提交/审核；DDL不可重复且有隐式提交，按脚本异常清单和阶段逐项核验。旧提交新增epoch/session/同人/封装为NULL隔离，旧批准不猜填生效快照。回滚应用先停新入口，保留新增结构及真实事实，不删除历史恢复旧流程。

已仅在受保护隔离库执行增量，独立随机所有权临时库比较旧基线升级、完整初始化及当前测试库：4表字段顺序/类型/默认/索引/CHECK和4菜单一致；旧资格及行政/听单不变、没有伪造review或snapshot。未修改业务/生产库。升级需重启后端、刷新后台菜单/统一授权，不需手动删Token缓存，环境根密钥和私有文件持久卷须保持。

## 实际验证

最终 **36类697项集成、12类41项单元，0失败/错误/跳过**。Q05专项55项；七类关联296项独立运行通过并由最终全量再次覆盖。全量后仅给既有自审用例补真实vi_event_log拒绝主体/action/reason断言，生产源码不变，再跑全部55专项及41单元通过。首次普通沙箱受Mockito本机附加限制，5单元错误且0业务IT，按既有本地测试提升模式重跑通过，未绕过断言。主业务、鉴权、数据库事务及资格Service未替换；仅受控Clock、微信测试运输及Redis/outbox故障接缝明确隔离。

两条完整链实际执行Q04设备签名/双人人工、Q03真实MP4/墙钟/阅卷、Q01真实提交封装、Q05审核和会话切身份；真实人工复核人可最终审核。其他测试明确建立上游完整消费边界，不把它当真实微信/硬件验证。三条后台实际响应同时校验主API与Q05切片，未知设备本人GET也校契约。

覆盖原/新公司并集、多部门非全局、旧Token/Redis、申请人历史手机号/关联防自审、公司A转B再转A、原登录退出、冻结封装/父事实篡改、UTC/上海边界、四CAS/UUID/并发终态、重复/旧到期任务/补缺、只读原图理由与票据、审计/outbox故障整体回滚。当前source TC-015和TC-R62-005由Q05负责并实际验证；TC-017主责任改D02，实际自动/人工派单入口仍后续，保持PARTIAL边界，不以基础Policy检查冒充全程派单。

PC17项、MINI7项、当前静态原型13项真实浏览器/明确API夹具共37项通过；UI不等于直连MySQL，原型不形成真实审批或任务。小程序类型、微信/H5及PC生产构建通过。额外PC全站vue-tsc仍129项（旧120+本轮同源Vue声明9），业务自身错误0，不冒称全站类型通过。34工程/42资料静态检查、SQL对比和可读性复核分别归档。

初轮testCompile两次未进入业务IT，不计通过；首次实际关联296中30失败，29为审核幂等意图直接序列化Instant，而隔离ViJson未启用时间模块。仅将9键意图的时刻转UTC文本，未更改共享Mapper或业务校验。另1为旧Q01不允许历史DRIVER入口断言，按本轮范围保留入口并增强失效资格/禁新单断言。独立审补了跨公司审核行及顶部工作态清理测试；最终296及全量697/41全部通过，详见verification-fixes.json。

PRD、真实UI、当前原型、F49图源/图片、主API/types/权限/枚举、模型/白名单/SQL、任务切片与账本同步。源用例仍NOT_RUN，真实运行状态在实施证据，不改写Q01–Q04历史报告。独立复核和最终指纹见review.md/reviewed-sources.json。

中文本地实现提交、实际SHA记账及任务标签见VERSIONS.md。本轮未推送GitHub、部署、发送真实通知、调用真实微信或进行资金操作。下一主线D01听单与定位；真实微信/硬件及生产验收继续L01/L02/L03。

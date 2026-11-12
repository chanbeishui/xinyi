# D01 前端、主原型与图源验证

日期：2026-11-12。本报告只记录实际前端验证，不替代后端HTTP/MySQL、真实微信平台或具体订单派单验收。

公司听单设置已接入 `/driver-management/listening-config`。管理员明确选择有权公司，完整填写每日下班、位置/近期可达有效期、最大位置误差、前台上报间隔与维护原因后，七键PUT按版本与UUID保存并立即生效。没有平台默认、上班时间或次日自动恢复；409保留填写重读，未知结果按原公司/原体/原键恢复，目录与公司业务403均失败关闭。

小程序实际入口为 `/pages/work/index`。当前DRIVER先重鉴权，再独立呈现听单意愿/有效截止、行政许可、实时资格、占用、位置时效/精度、近期联系及真实归还承接计数。开启冻结上海业务日及公司/配置/会话观察值，听单、位置和presence各用独立CAS。onHide仅尽力报告BACKGROUND并停止本页前台采点，不关闭听单，不把后台直接判为不可达。次日只接受本人手动开启；基础候选就绪不声称D02车型/半径匹配或接受成功。个人邀约详情与接受仍明确留在后续任务，未虚构邀约或收入。

前台位置使用微信 `uni.getLocation(type: gcj02)`，保守采用请求开始时刻作采点时间；精度向上取整，缺真实精度传null，不推断候选有效。最短30秒是系统约束，并非微信官方统一限额。H5未配置可信GCJ02服务，只看服务端投影。坐标只存在当前请求/未知原意图内存；角色、会话或上下文变化的订阅会立即清理，原生迟到回调不能上报。工作页补齐既有I03退出API的实际按钮（I02身份交互兼容）：未知保原UUID/凭据并停止本页采点，明确成功才清session，不另发关听单、解除承接或释放车辆责任。

## 实际结果

| 验证 | 结果 | 证据 |
| --- | --- | --- |
| PC真实页面/API夹具 | 12项通过；异常控制、公司隔离、CAS/UUID/未知恢复、只读、390布局 | pc-browser.json / pc-browser.log |
| MINI真实H5页面/API夹具 | 18项通过；前后台、跨日、旧原体、角色切换、服务计数缺失保护、403与真实退出按钮 | mini-browser.json / mini-browser.log |
| 实际MP构建模块/原生适配替身 | 7项通过；GCJ02、精度、节流、迟到、拒绝、非DRIVER、会话即时清理 | native-adapter.json / native-adapter.log |
| 主原型与F10原生图 | 7项通过；356画板、公司未知保存、权限场景、后台/到期/次日/在手与文字溢出检查 | prototype-browser.json / prototype-browser.log |
| PC生产构建 | exit 0 | ui-build.log / ui-commands.json |
| PC全站类型 | exit 2，134项=旧129项+新增同源Vue导出声明5项；本次业务自身错误0 | ui-type-summary.json / ui-type-check.log |
| MINI类型、MP及H5生产构建 | 全部exit 0 | mini-commands.json及对应日志 |

Browser plugin及其browser skill未列在本会话中，按已读取的frontend-testing-debugging技能使用已有npm缓存Playwright 1.61.1与本机Chrome，无仓库测试框架或依赖变更。PC URL `http://127.0.0.1:5173/driver-management/listening-config`，MINI URL `http://127.0.0.1:5175/#/pages/work/index`；1440×1000与390×844。所有页面非空、身份/路径正确、无框架错误覆盖；最终相关console error/warn及未知接口均为空，预期403/409/503单独记录。API及原生回调全部为明确夹具，不能证明真实后台授权、事务或真人GPS正确。

## 独立复核后的有效投影修订

初轮15项页面夹具没有覆盖读取/心跳失败后及手动关闭后停留至租约届满的缓存呈现，不能据初轮结果证明这些边界。修改前的MINI构建/GUI、原生适配及四张截图已原样保存在`initial-`前缀证据中，不覆盖历史检查结果。

本轮仅修改小程序工作页与DriverListening：所有失败（含GET503、心跳写未知、409及400/422）都撤回旧有效投影并停止前台采点，原未知正文/时间/UUID仍由独立意图容器保留。缓存另按服务端返回的locationExpiresAt、reachabilityExpiresAt及有效听单截止设置最早失效timer，与听单上报循环独立；即使手动关闭导致不再循环上报，也不长期展示旧位置/可达/绿色就绪。到期只清本地有效投影并要求重读，不发PATCH、不续租、不推断后台意愿开关或解除车辆责任。资格与行政等没有独立截止的缓存分项统一标“上次服务端核对”，未知状态不假显示持续加载。

最终18项保留原15项并新增：BASE_READY后GET503撤回旧事实；BASE_READY后前台presence503保留原键恢复且不重复记版本/延长联系时间；手动关闭后超过位置/联系TTL也撤回缓存且无任何新增写入。浏览器/API夹具时钟从2026-11-12北京时间12:00开始，分别快进31秒、120秒与一天，明确只验证前端状态失效/原意图恢复，不代替Java时间窗口、数据库事务或真实设备验收。最后小程序类型/MP/H5构建全0、18GUI及实际MP适配7项重跑通过；PC12项和主原型7项未因本修订重复计算或改动。

新图`mini-availability-unknown.png`、`mini-presence-unknown.png`展示旧绿色条件已撤回且原意图仍可恢复。其它四张实际MINI图已用最终代码更新；主原型、F10和PC证据保持原有验证范围。

## 源资料与图像

新增 `B24-listening-company` 为B24专用子页，归D01；B24其余C01范围没有因此标为完成。page_specs、prd_pages、画板索引、route-plan和源page_implementation_map同步；源目录355→356。D06三个状态保留个人邀约与本人服务单量范围：本轮真实完成归还计数和占用，个人邀约/接受明确由D02后续接通。

主PRD/身份规则与可点击 `dispatch_pages.js` 同步，主原型已实际构建并验证。F10的flows/Mermaid/DOT同步当前门槛；本环境无dot可执行文件，当前SVG前半部按同源节点手工原生文字排布，下半部保留既有订单设计且显式注明未在本轮验收。Chrome按1260×2310重渲PNG，新矩形节点文字边界检查无溢出，并目视核对。当前主图没有以旧PNG冒充重渲，历史实施证据未改。

八张实际页面/主原型证据为本目录 `pc-*.png`、`mini-*.png`及`prototype-*.png`。四画板的首屏与完整内容导出另见 `board-export.json`，两种高度分别实际捕获，不用同图复制替代。

## 官方依据与未验证范围

[微信getLocation官方文档](https://developers.weixin.qq.com/miniprogram/dev/api/location/wx.getLocation.html)说明普通定位离开小程序后不可调用，并要求接口开通、配置声明与频率约束；[uni-app定位文档](https://uniapp.dcloud.net.cn/api/location/location.html)说明GCJ02及H5服务配置差异。[微信隐私指南](https://developers.weixin.qq.com/miniprogram/dev/framework/user-privacy/PrivacyAuthorize.html)要求平台配置隐私处理范围并同步用户同意，第六节说明默认官方隐私弹窗。原文通过官方站点只读获取核对，开发代码不伪造平台同意。

真实微信类目/位置接口开通、已发布隐私保护指引、首次同意/拒绝、真机GPS精度、前后台事件送达与弱网/系统强退仍需上线门禁验证；浏览器及原生适配替身不算真机通过。没有测试或声称D02特定订单派单/接受、真实后台持续定位、生产自动关闭定时器实际运行或真实资金操作。

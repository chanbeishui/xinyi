# 视觉设计规范 V6.4 · Apple HIG参考与跨端适配

## 1 方向与来源

采用清晰的内容层级、安静的导航、可辨识的操作反馈，而不是把所有卡片做成玻璃。Apple HIG是视觉与交互参考，本产品仍是微信小程序和PC网页，不是原生iOS/macOS应用，不声称Apple认证。

以下为已确认视觉体系，V6.4沿用并定点更新身份相关界面；不是本轮重画全量页面。车主首页以一个明确预约动作开头；运营后台使用资料列表与详情，不以营销大卡片代替业务操作。车主、代驾、检测身份均用统一蓝色主要操作；角色通过名称和权限识别，不再用大面积蓝/绿/橙背景代替身份。

## 2 项目设计令牌

|令牌|值|用途|
|---|---|---|
|canvas|#F5F5F7|内容画布|
|surface|#FFFFFF|不透明工作面，表格/审核/金额|
|textPrimary|#1D1D1F|正文、表单和值|
|textSecondary|#626268|非关键辅助文字|
|accent|#0066CC|主要动作、链接、选中状态|
|separator|#E5E5E9|列表分隔；重要输入边界用更深色|
|success|#207344|成功，必须同时有文字|
|warning|#96530C|等待、差异，需要处理但非已失败|
|error|#B72E27|错误/拒绝；不能只依靠颜色|
|radius.panel|18–20px|内容容器|
|radius.primary|12px|小程序主要按钮|
|spacing|4/8/12/16/20/24/32px|相邻、组内、组间、页面间距|

字体使用操作系统现有字体：-apple-system/BlinkMacSystemFont/PingFang SC/Microsoft YaHei/Noto Sans CJK SC/system-ui。**不分发Apple字体或任何容器字体文件**；Windows显示系统中文回退字体是预期行为。

小程序：正文及输入16–17px，次级说明13px，关键金额28–38px，页面大标题28–32px。重要状态、地址、取消后果不得缩为浅色10px。44×44 CSS px是本项目触控目标；实际微信实现需根据rpx换算与屏幕密度适配，不能机械把points当CSS像素。

PC：页标题28–30px，正文/表格13–14px，辅助12px；主要文字不依赖字号很小的灰字。表头固定语义、金额右对齐并使用等宽数字，长订单号允许换行或完整提示，不能覆盖操作列。

## 3 导航层与内容层

顶部工具栏和手机底部导航可使用轻度透明/模糊；表格、金额、审核意见、对账单保持不透明。减少透明模式提供实色回退。减少动态关闭非必要位移与转场；有加载状态时仍保留文字说明。图表不是本产品主视觉，不在每个管理页添加无关趋势图。

资料与人员固定为车辆列表、车主列表、代驾员列表、代驾对账四个直接入口。售后只有“取消与退款”一个工作台，不把退款审核藏在另一组菜单。检测与配置、运行与审计分组保留，但不复制框架人员角色体系。

## 4 页面解剖

列表：页面标题/说明 → 操作 → 筛选 → 数据表 → 分页。查询结果保留到从详情返回；点击某一条打开该ID，不默认第一条。

详情：标识信息 → 状态 → 按任务组织内容 → 历史与关联记录 → 当前合法操作。车辆/车主/代驾是不同对象，不把一个大订单页当所有档案详情。

审核：申请与当前事实、金额可退额并排呈现；统一提交前二次确认说明“将取消”“将等待返还”“仅退款”等后果。主按钮用明确动词，不全部写“确认”。

## 5 适配与辅助设置

手机检查375、390、428px预览；后续微信真机必须覆盖底部安全区、原生胶囊、软键盘、系统字体变大、相机和外部导航返回。后台以1440px设计，1280/1024下允许表格内部横滚，不挤成不可读卡片。手机浏览原型不是承诺PC后台有完整手机管理版。

提供深色演示、增加对比、增大文字、减少透明和减少动态入口。深色与大字的关键页面是设计样本；本次不宣称所有设备/读屏/深色组合均已全面认证。

## 6 Apple官方参考

- Apple Human Interface Guidelines — Layout: https://developer.apple.com/design/human-interface-guidelines/layout
- Accessibility: https://developer.apple.com/design/human-interface-guidelines/accessibility
- Materials: https://developer.apple.com/design/human-interface-guidelines/materials
- Buttons: https://developer.apple.com/design/human-interface-guidelines/buttons
- Sheets: https://developer.apple.com/design/human-interface-guidelines/sheets
- Entering data: https://developer.apple.com/design/human-interface-guidelines/entering-data
- Design tips: https://developer.apple.com/design/tips/

参考核验日期：2026-10-10。布局和材料用于设计原则，具体色值/字号/圆角是本项目取值。没有复制Apple标志、字体文件或原生组件代码。


本版复用此前已确认的视觉体系，不将旧模块JS文件名中的v4/v41/v42当成活动需求版本。所有画板从本版index与catalog重建；新增培训、申诉、绑定页同规范。

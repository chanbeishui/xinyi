/**
 * 可视化大屏演示数据
 *
 * 全部为前端 mock，后期对接若依后端时只需把 provinceSites / citySites / activityTemplates
 * 换成接口返回（字段保持一致），页面与地图组件无需调整。
 */

import type { LngLat } from './geoData'

/** 业务类型（弧线颜色按此区分） */
export type BizType = '材料检测' | '结构检测' | '食品检测' | '环境检测' | '计量校准'

/** 业务点运行状态 */
export type SiteStatus = 'running' | 'busy' | 'warning' | 'done'

/** 业务点任务 */
export interface SiteTask {
  name: string
  date: string
  status: '进行中' | '待复核' | '已完成' | '已预警'
}

/** 业务点 */
export interface BizSite {
  id: string
  /** 项目/网点名称 */
  name: string
  /** 所属上级行政区（省级视图为地市名，市级视图为区县名） */
  region: string
  /**
   * 落点所在的下级区划简称：省级视图为区县（如"新会""茂南"），
   * 市级视图为镇/街道（如"镇隆""博贺"）。地图点位上标注的就是这个字段。
   */
  county: string
  /** 所在层级 */
  level: 'province' | 'city'
  /** 落点经纬度（取 county 的位置，而非地市政府所在地） */
  center: LngLat
  type: BizType
  status: SiteStatus
  /** 在检项目数 */
  projects: number
  /** 送检样品数 */
  samples: number
  /** 已出报告数 */
  reports: number
  /** 合同额（万元） */
  amount: number
  /** 项目进度 % */
  progress: number
  /** 驻场人数 */
  staff: number
  /** 负责人 */
  manager: string
  /** 进场日期 */
  startDate: string
  /** 与信宜中心的直线距离（km，演示值） */
  distance: number
  tasks: SiteTask[]
}

/** 状态样式元数据 */
export const statusMeta: Record<SiteStatus, { label: string; color: string }> = {
  running: { label: '正常运行', color: '#36cfc9' },
  busy: { label: '满负荷', color: '#faad14' },
  warning: { label: '预警关注', color: '#ff4d4f' },
  done: { label: '已完工', color: '#52c41a' }
}

/** 业务类型配色：地图弧线、节点、图例统一取这里 */
export const typeMeta: Record<BizType, string> = {
  材料检测: '#722ed1',
  结构检测: '#1677ff',
  食品检测: '#52c41a',
  环境检测: '#36cfc9',
  计量校准: '#faad14'
}

/** 业务类型列表（图例 / 筛选用） */
export const bizTypes = Object.keys(typeMeta) as BizType[]

// ========== 一级：广东省内跨市业务点 ==========

export const provinceSites: BizSite[] = [
  {
    id: 'P-GZ-01',
    name: '南沙港三期码头结构检测',
    region: '广州',
    county: '南沙',
    level: 'province',
    center: [113.55, 22.79],
    type: '结构检测',
    status: 'busy',
    projects: 18,
    samples: 1462,
    reports: 1198,
    amount: 386,
    progress: 72,
    staff: 16,
    manager: '梁俊铭',
    startDate: '2026-01-12',
    distance: 268,
    tasks: [
      { name: '码头桩基完整性检测', date: '08-04', status: '进行中' },
      { name: 'C50 混凝土抗压强度复检', date: '08-03', status: '待复核' },
      { name: '钢筋原材进场验收', date: '08-01', status: '已完成' }
    ]
  },
  {
    id: 'P-SZ-01',
    name: '前海综合枢纽结构监测',
    region: '深圳',
    county: '前海',
    level: 'province',
    center: [113.9, 22.53],
    type: '结构检测',
    status: 'running',
    projects: 12,
    samples: 936,
    reports: 802,
    amount: 452,
    progress: 65,
    staff: 14,
    manager: '陈立航',
    startDate: '2026-02-03',
    distance: 336,
    tasks: [
      { name: '地铁联络通道沉降观测', date: '08-05', status: '进行中' },
      { name: '基坑支护应力监测', date: '08-04', status: '进行中' },
      { name: '主体结构实体检测', date: '07-29', status: '已完成' }
    ]
  },
  {
    id: 'P-ZH-01',
    name: '金湾航空新城材料试验',
    region: '珠海',
    county: '金湾',
    level: 'province',
    center: [113.36, 22.06],
    type: '材料检测',
    status: 'running',
    projects: 9,
    samples: 726,
    reports: 640,
    amount: 214,
    progress: 58,
    staff: 10,
    manager: '苏敬文',
    startDate: '2026-03-06',
    distance: 288,
    tasks: [
      { name: '预应力管桩抽芯检测', date: '08-05', status: '进行中' },
      { name: '回填土压实度检测', date: '08-02', status: '已完成' }
    ]
  },
  {
    id: 'P-FS-01',
    name: '顺德建陶产业材料检测',
    region: '佛山',
    county: '顺德',
    level: 'province',
    center: [113.29, 22.81],
    type: '材料检测',
    status: 'running',
    projects: 8,
    samples: 1044,
    reports: 968,
    amount: 176,
    progress: 82,
    staff: 8,
    manager: '何嘉伟',
    startDate: '2025-11-18',
    distance: 246,
    tasks: [
      { name: '陶瓷砖吸水率与强度检测', date: '08-05', status: '进行中' },
      { name: '釉面砖抗釉裂性检验', date: '08-01', status: '待复核' }
    ]
  },
  {
    id: 'P-DG-01',
    name: '虎门跨江通道计量校准',
    region: '东莞',
    county: '虎门',
    level: 'province',
    center: [113.68, 22.82],
    type: '计量校准',
    status: 'busy',
    projects: 11,
    samples: 512,
    reports: 448,
    amount: 262,
    progress: 61,
    staff: 12,
    manager: '吴伟东',
    startDate: '2026-01-26',
    distance: 282,
    tasks: [
      { name: '索力测试设备周期校准', date: '08-05', status: '进行中' },
      { name: '静力水准仪比对校准', date: '08-03', status: '待复核' },
      { name: '拉力试验机示值校准', date: '07-27', status: '已完成' }
    ]
  },
  {
    id: 'P-HZ-01',
    name: '大亚湾石化园区环境检测',
    region: '惠州',
    county: '大亚湾',
    level: 'province',
    center: [114.55, 22.75],
    type: '环境检测',
    status: 'warning',
    projects: 15,
    samples: 1725,
    reports: 1602,
    amount: 298,
    progress: 88,
    staff: 12,
    manager: '吴建霖',
    startDate: '2025-12-01',
    distance: 356,
    tasks: [
      { name: '厂界无组织废气监测', date: '08-05', status: '已预警' },
      { name: '园区雨水排口水质检测', date: '08-03', status: '进行中' },
      { name: '土壤挥发性有机物筛查', date: '07-28', status: '已完成' }
    ]
  },
  {
    id: 'P-JM-01',
    name: '新会轨道交通材料复检',
    region: '江门',
    county: '新会',
    level: 'province',
    center: [113.03, 22.47],
    type: '材料检测',
    status: 'running',
    projects: 7,
    samples: 588,
    reports: 502,
    amount: 168,
    progress: 54,
    staff: 9,
    manager: '关志明',
    startDate: '2026-03-16',
    distance: 232,
    tasks: [
      { name: '轨道扣件疲劳性能试验', date: '08-04', status: '进行中' },
      { name: '道床混凝土配比验证', date: '08-01', status: '待复核' }
    ]
  },
  {
    id: 'P-ZJ-01',
    name: '廉江红橙产业食品检测',
    region: '湛江',
    county: '廉江',
    level: 'province',
    center: [110.28, 21.61],
    type: '食品检测',
    status: 'running',
    projects: 10,
    samples: 1362,
    reports: 1244,
    amount: 226,
    progress: 76,
    staff: 11,
    manager: '林启超',
    startDate: '2025-10-20',
    distance: 92,
    tasks: [
      { name: '果品农药残留筛查', date: '08-05', status: '进行中' },
      { name: '果汁饮料微生物指标检验', date: '08-02', status: '待复核' },
      { name: '包装材料迁移量试验', date: '07-25', status: '已完成' }
    ]
  },
  {
    id: 'P-MM-01',
    name: '茂名滨海港区材料检测',
    region: '茂名',
    county: '电白',
    level: 'province',
    center: [111.08, 21.53],
    type: '材料检测',
    status: 'running',
    projects: 8,
    samples: 684,
    reports: 596,
    amount: 188,
    progress: 64,
    staff: 9,
    manager: '陈海峰',
    startDate: '2026-02-26',
    distance: 96,
    tasks: [
      { name: '码头混凝土耐久性检测', date: '08-06', status: '进行中' },
      { name: '港区钢构件涂层厚度复检', date: '08-03', status: '待复核' }
    ]
  },
  {
    id: 'P-YJ-01',
    name: '阳西海上风电基础检测',
    region: '阳江',
    county: '阳西',
    level: 'province',
    center: [111.62, 21.75],
    type: '结构检测',
    status: 'busy',
    projects: 13,
    samples: 864,
    reports: 736,
    amount: 408,
    progress: 69,
    staff: 15,
    manager: '黄志海',
    startDate: '2025-12-08',
    distance: 108,
    tasks: [
      { name: '单桩基础垂直度复测', date: '08-05', status: '进行中' },
      { name: '灌浆连接段强度检测', date: '08-04', status: '进行中' },
      { name: '塔筒法兰螺栓预紧力检测', date: '07-30', status: '已完成' }
    ]
  },
  {
    id: 'P-SG-01',
    name: '南雄矿区土壤环境检测',
    region: '韶关',
    county: '南雄',
    level: 'province',
    center: [114.31, 25.12],
    type: '环境检测',
    status: 'running',
    projects: 6,
    samples: 642,
    reports: 566,
    amount: 154,
    progress: 63,
    staff: 8,
    manager: '罗永康',
    startDate: '2026-02-22',
    distance: 402,
    tasks: [
      { name: '重金属背景值加密采样', date: '08-04', status: '进行中' },
      { name: '地下水监测井水位观测', date: '08-01', status: '已完成' }
    ]
  },
  {
    id: 'P-ZQ-01',
    name: '高要装配式构件材料检测',
    region: '肇庆',
    county: '高要',
    level: 'province',
    center: [112.46, 23.03],
    type: '材料检测',
    status: 'running',
    projects: 9,
    samples: 812,
    reports: 704,
    amount: 196,
    progress: 67,
    staff: 10,
    manager: '梁广荣',
    startDate: '2026-01-05',
    distance: 176,
    tasks: [
      { name: '叠合板套筒灌浆料强度试验', date: '08-05', status: '进行中' },
      { name: '预制构件保护层厚度检测', date: '08-02', status: '待复核' }
    ]
  },
  {
    id: 'P-YF-01',
    name: '罗定稻米加工食品检测',
    region: '云浮',
    county: '罗定',
    level: 'province',
    center: [111.57, 22.77],
    type: '食品检测',
    status: 'done',
    projects: 5,
    samples: 968,
    reports: 968,
    amount: 132,
    progress: 100,
    staff: 6,
    manager: '陈丽珊',
    startDate: '2025-09-15',
    distance: 74,
    tasks: [
      { name: '大米镉含量检测', date: '07-30', status: '已完成' },
      { name: '真菌毒素限量检验', date: '07-26', status: '已完成' }
    ]
  },
  {
    id: 'P-QY-01',
    name: '英德水泥熟料材料检测',
    region: '清远',
    county: '英德',
    level: 'province',
    center: [113.4, 24.18],
    type: '材料检测',
    status: 'busy',
    projects: 12,
    samples: 1186,
    reports: 1042,
    amount: 288,
    progress: 74,
    staff: 13,
    manager: '刘泽楷',
    startDate: '2025-11-06',
    distance: 312,
    tasks: [
      { name: '熟料胶砂强度试验', date: '08-05', status: '进行中' },
      { name: '水泥安定性检验', date: '08-03', status: '进行中' },
      { name: '骨料碱活性试验', date: '07-29', status: '已完成' }
    ]
  },
  {
    id: 'P-ZS-01',
    name: '中山火炬园区仪器计量校准',
    region: '中山',
    county: '火炬',
    level: 'province',
    center: [113.45, 22.55],
    type: '计量校准',
    status: 'running',
    projects: 8,
    samples: 476,
    reports: 428,
    amount: 148,
    progress: 59,
    staff: 7,
    manager: '周敏华',
    startDate: '2026-02-11',
    distance: 258,
    tasks: [
      { name: '恒温恒湿箱温湿度校准', date: '08-05', status: '进行中' },
      { name: '电子天平期间核查', date: '08-02', status: '已完成' }
    ]
  },
  {
    id: 'P-HY-01',
    name: '龙川农产品加工食品检测',
    region: '河源',
    county: '龙川',
    level: 'province',
    center: [115.26, 24.1],
    type: '食品检测',
    status: 'warning',
    projects: 7,
    samples: 894,
    reports: 762,
    amount: 142,
    progress: 71,
    staff: 8,
    manager: '曾振宏',
    startDate: '2026-01-19',
    distance: 396,
    tasks: [
      { name: '腌制食品亚硝酸盐检测', date: '08-05', status: '已预警' },
      { name: '食用油过氧化值检验', date: '08-03', status: '进行中' },
      { name: '生产用水微生物检验', date: '07-31', status: '已完成' }
    ]
  },
  {
    id: 'P-GX-WZ-01',
    name: '岑溪石材产业材料检测',
    region: '梧州',
    county: '岑溪',
    level: 'province',
    center: [111.0, 22.93],
    type: '材料检测',
    status: 'running',
    projects: 6,
    samples: 532,
    reports: 468,
    amount: 126,
    progress: 66,
    staff: 7,
    manager: '莫文杰',
    startDate: '2026-03-11',
    distance: 68,
    tasks: [
      { name: '饰面石材弯曲强度检测', date: '08-06', status: '进行中' },
      { name: '建筑用砂氯离子复检', date: '08-02', status: '已完成' }
    ]
  },
  {
    id: 'P-GX-YL-01',
    name: '北流陶瓷产业质量检测',
    region: '玉林',
    county: '北流',
    level: 'province',
    center: [110.35, 22.71],
    type: '材料检测',
    status: 'busy',
    projects: 9,
    samples: 846,
    reports: 724,
    amount: 174,
    progress: 73,
    staff: 9,
    manager: '陆嘉明',
    startDate: '2026-01-17',
    distance: 82,
    tasks: [
      { name: '陶瓷砖断裂模数检测', date: '08-06', status: '进行中' },
      { name: '烧结制品吸水率复检', date: '08-04', status: '待复核' }
    ]
  },
  {
    id: 'P-GX-QZ-01',
    name: '钦州港临港工程结构检测',
    region: '钦州',
    county: '钦南',
    level: 'province',
    center: [108.66, 21.98],
    type: '结构检测',
    status: 'running',
    projects: 7,
    samples: 516,
    reports: 438,
    amount: 206,
    progress: 61,
    staff: 8,
    manager: '黄子健',
    startDate: '2026-02-08',
    distance: 246,
    tasks: [
      { name: '港区堆场地基承载力检测', date: '08-06', status: '进行中' },
      { name: '引桥支座位移复测', date: '08-01', status: '已完成' }
    ]
  },
  {
    id: 'P-GX-NN-01',
    name: '南宁青秀实验室计量校准',
    region: '南宁',
    county: '青秀',
    level: 'province',
    center: [108.37, 22.82],
    type: '计量校准',
    status: 'warning',
    projects: 5,
    samples: 308,
    reports: 254,
    amount: 118,
    progress: 58,
    staff: 6,
    manager: '韦思远',
    startDate: '2026-03-20',
    distance: 276,
    tasks: [
      { name: '力学设备量值溯源校准', date: '08-06', status: '已预警' },
      { name: '温湿度巡检仪比对', date: '08-03', status: '进行中' }
    ]
  }
]

// ========== 二级：茂名市内业务网点 ==========

export const citySites: BizSite[] = [
  {
    id: 'C-XY-01',
    name: '信宜镇隆古城修缮结构检测',
    region: '信宜市',
    county: '镇隆',
    level: 'city',
    center: [110.87471, 22.207415],
    type: '结构检测',
    status: 'running',
    projects: 6,
    samples: 428,
    reports: 386,
    amount: 96,
    progress: 68,
    staff: 7,
    manager: '李文远',
    startDate: '2026-02-18',
    distance: 12,
    tasks: [
      { name: '砖木结构承载力评估', date: '08-05', status: '进行中' },
      { name: '古建灰浆配比分析', date: '08-02', status: '待复核' }
    ]
  },
  {
    id: 'C-XY-02',
    name: '钱排三华李冷链食品检测',
    region: '信宜市',
    county: '钱排',
    level: 'city',
    center: [111.255122, 22.371167],
    type: '食品检测',
    status: 'busy',
    projects: 9,
    samples: 1246,
    reports: 1104,
    amount: 118,
    progress: 81,
    staff: 9,
    manager: '梁翠芬',
    startDate: '2025-12-22',
    distance: 24,
    tasks: [
      { name: '鲜果农残快检', date: '08-05', status: '进行中' },
      { name: '冷库温湿度记录仪校核', date: '08-04', status: '进行中' },
      { name: '果脯二氧化硫残留检测', date: '07-31', status: '已完成' }
    ]
  },
  {
    id: 'C-XY-03',
    name: '怀乡水厂供水环境检测',
    region: '信宜市',
    county: '怀乡',
    level: 'city',
    center: [111.0543, 22.48106],
    type: '环境检测',
    status: 'running',
    projects: 4,
    samples: 366,
    reports: 332,
    amount: 62,
    progress: 57,
    staff: 5,
    manager: '张兆棠',
    startDate: '2026-03-09',
    distance: 21,
    tasks: [
      { name: '出厂水常规指标检测', date: '08-05', status: '进行中' },
      { name: '管网末梢余氯监测', date: '08-01', status: '已完成' }
    ]
  },
  {
    id: 'C-GZ-01',
    name: '高州石鼓产业园材料检测',
    region: '高州市',
    county: '石鼓',
    level: 'city',
    center: [110.772332, 21.822757],
    type: '材料检测',
    status: 'running',
    projects: 8,
    samples: 762,
    reports: 688,
    amount: 134,
    progress: 72,
    staff: 9,
    manager: '车俊贤',
    startDate: '2026-01-08',
    distance: 56,
    tasks: [
      { name: '路面基层水泥稳定碎石检测', date: '08-05', status: '进行中' },
      { name: '钢筋焊接接头拉伸试验', date: '08-03', status: '待复核' }
    ]
  },
  {
    id: 'C-GZ-02',
    name: '大坡桥梁加固结构检测',
    region: '高州市',
    county: '大坡',
    level: 'city',
    center: [111.24144, 22.08465],
    type: '结构检测',
    status: 'warning',
    projects: 5,
    samples: 288,
    reports: 232,
    amount: 88,
    progress: 64,
    staff: 6,
    manager: '邓志强',
    startDate: '2025-11-27',
    distance: 42,
    tasks: [
      { name: '主梁裂缝观测', date: '08-05', status: '已预警' },
      { name: '支座位移复测', date: '08-02', status: '进行中' }
    ]
  },
  {
    id: 'C-HZ-01',
    name: '化州平定化橘红食品检测',
    region: '化州市',
    county: '平定',
    level: 'city',
    center: [110.41013, 22.0265],
    type: '食品检测',
    status: 'running',
    projects: 7,
    samples: 946,
    reports: 862,
    amount: 104,
    progress: 76,
    staff: 8,
    manager: '钟惠玲',
    startDate: '2025-10-30',
    distance: 58,
    tasks: [
      { name: '化橘红有效成分含量测定', date: '08-05', status: '进行中' },
      { name: '干果重金属限量检验', date: '08-01', status: '待复核' }
    ]
  },
  {
    id: 'C-HZ-02',
    name: '化州河西安置区材料检测',
    region: '化州市',
    county: '河西',
    level: 'city',
    center: [110.62644, 21.656537],
    type: '材料检测',
    status: 'busy',
    projects: 10,
    samples: 884,
    reports: 742,
    amount: 156,
    progress: 69,
    staff: 11,
    manager: '陈耀东',
    startDate: '2026-01-20',
    distance: 78,
    tasks: [
      { name: '商品混凝土抗压强度检测', date: '08-05', status: '进行中' },
      { name: '砌体砂浆强度检测', date: '08-04', status: '进行中' },
      { name: '保温板导热系数检测', date: '07-30', status: '已完成' }
    ]
  },
  {
    id: 'C-MN-01',
    name: '茂南城区管网环境检测',
    region: '茂南区',
    county: '茂南',
    level: 'city',
    center: [110.918566, 21.641661],
    type: '环境检测',
    status: 'running',
    projects: 11,
    samples: 1128,
    reports: 1004,
    amount: 182,
    progress: 74,
    staff: 12,
    manager: '苏永成',
    startDate: '2025-12-15',
    distance: 82,
    tasks: [
      { name: '污水处理厂出水水质检测', date: '08-05', status: '进行中' },
      { name: '城区噪声昼夜等效声级监测', date: '08-03', status: '进行中' },
      { name: '扬尘在线监测设备比对', date: '07-28', status: '已完成' }
    ]
  },
  {
    id: 'C-DB-01',
    name: '博贺港区结构安全检测',
    region: '电白区',
    county: '博贺',
    level: 'city',
    center: [111.232357, 21.488027],
    type: '结构检测',
    status: 'busy',
    projects: 9,
    samples: 624,
    reports: 528,
    amount: 168,
    progress: 66,
    staff: 10,
    manager: '黎家俊',
    startDate: '2026-02-02',
    distance: 98,
    tasks: [
      { name: '码头面板氯离子含量检测', date: '08-05', status: '进行中' },
      { name: '钢栈桥焊缝超声检测', date: '08-04', status: '待复核' }
    ]
  },
  {
    id: 'C-DB-02',
    name: '沙琅罗非鱼加工食品检测',
    region: '电白区',
    county: '沙琅',
    level: 'city',
    center: [111.221982, 21.776268],
    type: '食品检测',
    status: 'done',
    projects: 4,
    samples: 706,
    reports: 706,
    amount: 74,
    progress: 100,
    staff: 5,
    manager: '吴嘉宁',
    startDate: '2025-09-08',
    distance: 76,
    tasks: [
      { name: '水产品药物残留检测', date: '07-29', status: '已完成' },
      { name: '速冻食品中心温度核查', date: '07-24', status: '已完成' }
    ]
  },
  {
    id: 'C-DB-03',
    name: '滨海新区实验室计量校准',
    region: '电白区',
    county: '电城',
    level: 'city',
    center: [111.290618, 21.5118],
    type: '计量校准',
    status: 'running',
    projects: 6,
    samples: 342,
    reports: 316,
    amount: 92,
    progress: 62,
    staff: 6,
    manager: '柯明辉',
    startDate: '2026-03-02',
    distance: 104,
    tasks: [
      { name: '万能试验机力值校准', date: '08-05', status: '进行中' },
      { name: '标准养护室温湿度校准', date: '08-02', status: '已完成' }
    ]
  }
]

// ========== 底部指标 ==========

export interface KpiItem {
  key: string
  label: string
  value: number
  unit: string
  icon: string
  color: string
}

/** 由业务点列表实时汇总底部 KPI，切换层级后自动跟随 */
export function buildKpis(sites: BizSite[]): KpiItem[] {
  const sum = (fn: (s: BizSite) => number) => sites.reduce((acc, s) => acc + fn(s), 0)
  const regions = new Set(sites.map((s) => s.region))

  return [
    { key: 'sites', label: '业务点', value: sites.length, unit: '个', icon: 'guide', color: '#1677ff' },
    { key: 'regions', label: '覆盖行政区', value: regions.size, unit: '个', icon: 'international', color: '#36cfc9' },
    { key: 'projects', label: '在检项目', value: sum((s) => s.projects), unit: '项', icon: 'list', color: '#722ed1' },
    { key: 'samples', label: '送检样品', value: sum((s) => s.samples), unit: '件', icon: 'archive', color: '#13c2c2' },
    {
      key: 'reports',
      label: '已出报告',
      value: sum((s) => s.reports),
      unit: '份',
      icon: 'documentation',
      color: '#52c41a'
    },
    { key: 'amount', label: '合同额', value: sum((s) => s.amount), unit: '万元', icon: 'money', color: '#faad14' },
    { key: 'staff', label: '驻场人员', value: sum((s) => s.staff), unit: '人', icon: 'peoples', color: '#eb2f96' }
  ]
}

// ========== 实时动态 ==========

export interface ActivityItem {
  id: string
  time: string
  region: string
  text: string
  level: 'info' | 'success' | 'warning'
}

const activityTemplates: { text: string; level: ActivityItem['level'] }[] = [
  { text: '新增送检样品 12 件，已进入待检队列', level: 'info' },
  { text: '完成现场取样并上传原始记录', level: 'info' },
  { text: '检测报告已签发，同步至业务系统', level: 'success' },
  { text: '样品检测结果全部合格', level: 'success' },
  { text: '关键指标超出限值，已触发复检流程', level: 'warning' },
  { text: '设备期间核查异常，暂停相关项目', level: 'warning' },
  { text: '驻场人员完成资质换证备案', level: 'info' },
  { text: '本月产值达成率超过计划值', level: 'success' },
  { text: '样品交接超时预警，请及时处理', level: 'warning' }
]

let activitySeq = 0

/** 生成一条动态；secondsAgo 用于把首屏的多条动态在时间轴上错开 */
export function nextActivity(sites: BizSite[], secondsAgo = 0): ActivityItem {
  const site = sites[Math.floor(Math.random() * sites.length)]
  const tpl = activityTemplates[Math.floor(Math.random() * activityTemplates.length)]
  const at = new Date(Date.now() - secondsAgo * 1000)
  activitySeq += 1

  return {
    id: `A-${activitySeq}`,
    time: at.toLocaleTimeString('zh-CN', { hour12: false }),
    region: site ? `${site.region}·${site.county}` : '—',
    text: tpl.text,
    level: tpl.level
  }
}

/** 首屏动态列表（时间由近到远） */
export function initActivities(sites: BizSite[], count = 8): ActivityItem[] {
  return Array.from({ length: count }, (_, i) => nextActivity(sites, (i + 1) * 37))
}

// ========== 接口定义 ==========

/** 档案柜 */
export interface Cabinet {
  id: string
  zone: string
  row: number
  col: number
  x: number
  y: number
  width: number
  height: number
  capacity: number
  used: number
  label: string
}

/** 档案文件 */
export interface ArchiveFile {
  id: string
  name: string
  cabinetId: string
  category: string
  year: string
  boxNumber: string
  status: 'normal' | 'borrowed' | 'missing'
}

/** 区配置 */
export interface ZoneConfig {
  zone: string
  startX: number
  startY: number
  label: string
  color: string
}

/** 档案室 */
export interface ArchiveRoom {
  id: string
  name: string
  location: string
  description: string
  zoneConfigs: ZoneConfig[]
  cabinets: Cabinet[]
  files: ArchiveFile[]
}

// ========== 常量 ==========

export const SVG_WIDTH = 1000
export const SVG_HEIGHT = 620

const CAB_W = 90
const CAB_H = 52
const CAB_GAP_X = 18
const CAB_GAP_Y = 22

// ========== 工具函数 ==========

/** 根据区配置生成柜子（每区 2 排 × 4 列 = 8 个） */
function generateCabinetsForZones(zones: ZoneConfig[], roomId: string): Cabinet[] {
  const usageSeed: Record<string, number[]> = {}
  for (const zc of zones) {
    usageSeed[zc.zone] = []
    for (let i = 0; i < 8; i++) {
      const seed = (i * 7 + zc.zone.charCodeAt(0) * 3 + roomId.charCodeAt(0) * 5) % 30
      usageSeed[zc.zone].push(28 + seed)
    }
  }

  const list: Cabinet[] = []
  for (const zc of zones) {
    for (let row = 1; row <= 2; row++) {
      for (let col = 1; col <= 4; col++) {
        const num = (row - 1) * 4 + col
        const id = `${zc.zone}-${String(num).padStart(2, '0')}`
        list.push({
          id,
          zone: zc.zone,
          row,
          col,
          x: zc.startX + (col - 1) * (CAB_W + CAB_GAP_X),
          y: zc.startY + (row - 1) * (CAB_H + CAB_GAP_Y),
          width: CAB_W,
          height: CAB_H,
          capacity: 60,
          used: usageSeed[zc.zone][num - 1],
          label: id,
        })
      }
    }
  }
  return list
}

/** 查找区的颜色 */
export function zoneColor(zone: string, zones: ZoneConfig[]): string {
  const zc = zones.find(z => z.zone === zone)
  return zc ? zc.color : '#8c8c8c'
}

/** 查找区的标签 */
export function zoneLabel(zone: string, zones: ZoneConfig[]): string {
  const zc = zones.find(z => z.zone === zone)
  return zc ? zc.label : zone
}

// ========== 1 号档案室 — 信宜总部主档案室 ==========

const room1Zones: ZoneConfig[] = [
  { zone: 'A', startX: 70,  startY: 70,  label: 'A 区 · 材料检测部',        color: '#1677ff' },
  { zone: 'B', startX: 560, startY: 70,  label: 'B 区 · 监测与结构检测部',  color: '#faad14' },
  { zone: 'C', startX: 70,  startY: 350, label: 'C 区 · 路桥与水利检测部',  color: '#13c2c2' },
  { zone: 'D', startX: 560, startY: 350, label: 'D 区 · 基础检测部',        color: '#722ed1' },
]

const room1Files: ArchiveFile[] = [
  { id: 'ARC-2026-0001', name: '中建三局混凝土抗压强度检测报告', cabinetId: 'A-01', category: '建筑工程', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0002', name: '中铁建工钢筋拉伸试验报告', cabinetId: 'A-01', category: '建筑工程', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-2026-0003', name: '万科地产桩基检测报告', cabinetId: 'A-02', category: '建筑工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0004', name: '碧桂园钢结构焊接检测报告', cabinetId: 'A-02', category: '建筑工程', year: '2026', boxNumber: '第4盒', status: 'borrowed' },
  { id: 'ARC-2026-0005', name: '中建八局水泥安定性检测报告', cabinetId: 'A-03', category: '建筑工程', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0006', name: '保利地产砂石骨料检测报告', cabinetId: 'A-03', category: '建筑工程', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-2026-0007', name: '华润置地混凝土配合比设计报告', cabinetId: 'A-04', category: '建筑工程', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0008', name: '中海地产防水卷材检测报告', cabinetId: 'A-04', category: '建筑工程', year: '2026', boxNumber: '第7盒', status: 'normal' },
  { id: 'ARC-2026-0009', name: '龙湖地产砌块抗压检测报告', cabinetId: 'A-05', category: '建筑工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0010', name: '招商蛇口外墙保温材料检测报告', cabinetId: 'A-05', category: '建筑工程', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-2026-0011', name: '金地集团门窗气密性检测报告', cabinetId: 'A-06', category: '建筑工程', year: '2026', boxNumber: '第2盒', status: 'borrowed' },
  { id: 'ARC-2026-0012', name: '融创中国钢结构防火涂料检测报告', cabinetId: 'A-06', category: '建筑工程', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-2026-0013', name: '绿地控股回填土压实度检测报告', cabinetId: 'A-07', category: '建筑工程', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0014', name: '世茂房地产钢筋焊接接头检测报告', cabinetId: 'A-07', category: '建筑工程', year: '2026', boxNumber: '第8盒', status: 'normal' },
  { id: 'ARC-2026-0015', name: '中南建设预应力混凝土检测报告', cabinetId: 'A-08', category: '建筑工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0016', name: '中交一公局沥青混合料检测报告', cabinetId: 'B-01', category: '公路水运', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-2026-0017', name: '广东省路桥集团路基压实度检测报告', cabinetId: 'B-01', category: '公路水运', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0018', name: '中交二航局桥梁支座检测报告', cabinetId: 'B-02', category: '公路水运', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-2026-0019', name: '中铁大桥局混凝土桥梁检测报告', cabinetId: 'B-02', category: '公路水运', year: '2026', boxNumber: '第1盒', status: 'borrowed' },
  { id: 'ARC-2026-0020', name: '广东交通集团隧道衬砌检测报告', cabinetId: 'B-03', category: '公路水运', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-2026-0021', name: '中交四航局水运工程检测报告', cabinetId: 'B-03', category: '公路水运', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0022', name: '广州港集团码头结构检测报告', cabinetId: 'B-04', category: '公路水运', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0023', name: '深圳盐田港集装箱堆场检测报告', cabinetId: 'B-04', category: '公路水运', year: '2026', boxNumber: '第7盒', status: 'normal' },
  { id: 'ARC-2026-0024', name: '广东冠粤公路沥青路面检测报告', cabinetId: 'B-05', category: '公路水运', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-2026-0025', name: '中交三航局港口工程检测报告', cabinetId: 'B-05', category: '公路水运', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0026', name: '中铁隧道集团盾构管片检测报告', cabinetId: 'B-06', category: '公路水运', year: '2026', boxNumber: '第5盒', status: 'borrowed' },
  { id: 'ARC-2026-0027', name: '广东省航道事务中心航道检测报告', cabinetId: 'B-06', category: '公路水运', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0028', name: '中交建集团路面标线检测报告', cabinetId: 'B-07', category: '公路水运', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0029', name: '广东路桥建设交通标志检测报告', cabinetId: 'B-07', category: '公路水运', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-2026-0030', name: '中铁建大桥工程局桩基检测报告', cabinetId: 'B-08', category: '公路水运', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-2026-0031', name: '广东省水利厅水库大坝安全检测报告', cabinetId: 'C-01', category: '水利工程', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0032', name: '中水北方水利勘测堤防检测报告', cabinetId: 'C-01', category: '水利工程', year: '2026', boxNumber: '第7盒', status: 'normal' },
  { id: 'ARC-2026-0033', name: '广东水利电力勘测设计院检测报告', cabinetId: 'C-02', category: '水利工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0034', name: '珠江水利委员会水文监测报告', cabinetId: 'C-02', category: '水利工程', year: '2026', boxNumber: '第5盒', status: 'borrowed' },
  { id: 'ARC-2026-0035', name: '茂名市食品检验所粮油检测报告', cabinetId: 'C-03', category: '食品检测', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0036', name: '广东省食品检验所乳制品检测报告', cabinetId: 'C-03', category: '食品检测', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-2026-0037', name: '广州质量检测院食品添加剂检测报告', cabinetId: 'C-04', category: '食品检测', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0038', name: '深圳海关食品检验中心检测报告', cabinetId: 'C-04', category: '食品检测', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-2026-0039', name: '湛江海鲜产品检测中心检测报告', cabinetId: 'C-05', category: '食品检测', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0040', name: '广东省水利科学研究院渗流检测报告', cabinetId: 'C-05', category: '水利工程', year: '2026', boxNumber: '第8盒', status: 'normal' },
  { id: 'ARC-2026-0041', name: '茂名市水质检测中心饮用水检测报告', cabinetId: 'C-06', category: '水利工程', year: '2026', boxNumber: '第3盒', status: 'borrowed' },
  { id: 'ARC-2026-0042', name: '信宜市食品检验所农产品检测报告', cabinetId: 'C-06', category: '食品检测', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0043', name: '广东电网输变电工程水土保持检测报告', cabinetId: 'C-07', category: '水利工程', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-2026-0044', name: '中水珠江规划勘测公司检测报告', cabinetId: 'C-07', category: '水利工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0045', name: '广东省食品检验所调味品检测报告', cabinetId: 'C-08', category: '食品检测', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-2026-0046', name: '集信国控2025年度质量管理体系文件', cabinetId: 'D-01', category: '综合管理', year: '2025', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0047', name: '集信国控资质证书及认定文件汇编', cabinetId: 'D-01', category: '综合管理', year: '2025', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0048', name: '检测设备校准证书汇编2026', cabinetId: 'D-02', category: '设备管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0049', name: '实验室CNAS认可申请材料', cabinetId: 'D-02', category: '综合管理', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-2026-0050', name: 'CMA资质认定评审材料', cabinetId: 'D-03', category: '综合管理', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0051', name: '检测人员培训考核记录汇编', cabinetId: 'D-03', category: '人员管理', year: '2026', boxNumber: '第4盒', status: 'borrowed' },
  { id: 'ARC-2026-0052', name: '客户投诉处理记录及整改报告', cabinetId: 'D-04', category: '综合管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0053', name: '内部审核及管理评审报告', cabinetId: 'D-04', category: '综合管理', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-2026-0054', name: '检测能力验证及比对结果报告', cabinetId: 'D-05', category: '综合管理', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-2026-0055', name: '2025年度检测业务统计报表', cabinetId: 'D-05', category: '综合管理', year: '2025', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-2026-0056', name: '汽车检测线设备验收报告', cabinetId: 'D-06', category: '设备管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-2026-0057', name: '实验室安全管理制度及应急预案', cabinetId: 'D-06', category: '综合管理', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-2026-0058', name: '检测报告质量抽查记录汇编', cabinetId: 'D-07', category: '综合管理', year: '2026', boxNumber: '第3盒', status: 'borrowed' },
  { id: 'ARC-2026-0059', name: '供应商评价及采购合同档案', cabinetId: 'D-07', category: '综合管理', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-2026-0060', name: '2026年度检测项目台账及合同', cabinetId: 'D-08', category: '综合管理', year: '2026', boxNumber: '第1盒', status: 'normal' },
]

// ========== 2 号档案室 — 茂名分部工程档案室 ==========

const room2Zones: ZoneConfig[] = [
  { zone: 'A', startX: 70,  startY: 70,  label: 'A 区 · 建筑工程档案',  color: '#1677ff' },
  { zone: 'B', startX: 560, startY: 70,  label: 'B 区 · 公路水运档案',  color: '#faad14' },
  { zone: 'C', startX: 315, startY: 350, label: 'C 区 · 水利工程档案',  color: '#13c2c2' },
]

const room2Files: ArchiveFile[] = [
  { id: 'ARC-MM-0001', name: '茂名市住建局建筑工程质量检测报告', cabinetId: 'A-01', category: '建筑工程', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-MM-0002', name: '茂名建工集团混凝土强度检测报告', cabinetId: 'A-01', category: '建筑工程', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-MM-0003', name: '电白建设集团钢筋力学检测报告', cabinetId: 'A-02', category: '建筑工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-MM-0004', name: '高州建总公司桩基完整性检测报告', cabinetId: 'A-02', category: '建筑工程', year: '2026', boxNumber: '第4盒', status: 'borrowed' },
  { id: 'ARC-MM-0005', name: '化州城建集团水泥物理性能检测报告', cabinetId: 'A-03', category: '建筑工程', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-MM-0006', name: '信宜建工砂石材料检测报告', cabinetId: 'A-03', category: '建筑工程', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-MM-0007', name: '茂名石化建安公司钢结构检测报告', cabinetId: 'A-04', category: '建筑工程', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-MM-0008', name: '广东茂化建集团防水材料检测报告', cabinetId: 'A-04', category: '建筑工程', year: '2026', boxNumber: '第7盒', status: 'normal' },
  { id: 'ARC-MM-0009', name: '茂名市公路局沥青路面检测报告', cabinetId: 'A-05', category: '建筑工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-MM-0010', name: '电白市政公司路基检测报告', cabinetId: 'A-05', category: '建筑工程', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-MM-0011', name: '高州住建局砌块检测报告', cabinetId: 'A-06', category: '建筑工程', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-MM-0012', name: '化州建总公司外墙保温检测报告', cabinetId: 'A-06', category: '建筑工程', year: '2026', boxNumber: '第2盒', status: 'borrowed' },
  { id: 'ARC-MM-0013', name: '茂名滨海度假区钢结构防腐检测报告', cabinetId: 'A-07', category: '建筑工程', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-MM-0014', name: '信宜市政公司门窗检测报告', cabinetId: 'A-07', category: '建筑工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-MM-0015', name: '茂名高新区回填土检测报告', cabinetId: 'A-08', category: '建筑工程', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-MM-0016', name: '茂名港集团港口码头结构检测报告', cabinetId: 'B-01', category: '公路水运', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-MM-0017', name: '广东滨海旅游公路茂名段检测报告', cabinetId: 'B-01', category: '公路水运', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-MM-0018', name: '茂名大道改扩建工程路面检测报告', cabinetId: 'B-02', category: '公路水运', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-MM-0019', name: 'G15沈海高速茂名段桥梁检测报告', cabinetId: 'B-02', category: '公路水运', year: '2026', boxNumber: '第4盒', status: 'borrowed' },
  { id: 'ARC-MM-0020', name: '茂名博贺新港区防波堤检测报告', cabinetId: 'B-03', category: '公路水运', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-MM-0021', name: '茂名港航道疏浚工程检测报告', cabinetId: 'B-03', category: '公路水运', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-MM-0022', name: '电白区农村公路路面检测报告', cabinetId: 'B-04', category: '公路水运', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-MM-0023', name: '高州水库公路桥梁检测报告', cabinetId: 'B-04', category: '公路水运', year: '2026', boxNumber: '第7盒', status: 'normal' },
  { id: 'ARC-MM-0024', name: '茂名市交通标志标线检测报告', cabinetId: 'B-05', category: '公路水运', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-MM-0025', name: '化州公路隧道衬砌检测报告', cabinetId: 'B-05', category: '公路水运', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-MM-0026', name: '茂名市国道养护工程检测报告', cabinetId: 'B-06', category: '公路水运', year: '2026', boxNumber: '第5盒', status: 'borrowed' },
  { id: 'ARC-MM-0027', name: '茂名综合客运枢纽路面检测报告', cabinetId: 'B-06', category: '公路水运', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-MM-0028', name: '茂名疏港公路路基检测报告', cabinetId: 'B-07', category: '公路水运', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-MM-0029', name: '电白区滨海公路检测报告', cabinetId: 'B-07', category: '公路水运', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-MM-0030', name: '茂名港集装箱堆场检测报告', cabinetId: 'B-08', category: '公路水运', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-MM-0031', name: '高州水库除险加固工程检测报告', cabinetId: 'C-01', category: '水利工程', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-MM-0032', name: '茂名市鉴江流域堤防检测报告', cabinetId: 'C-01', category: '水利工程', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-MM-0033', name: '罗坑水库大坝安全检测报告', cabinetId: 'C-02', category: '水利工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-MM-0034', name: '茂名市引水工程质量检测报告', cabinetId: 'C-02', category: '水利工程', year: '2026', boxNumber: '第4盒', status: 'borrowed' },
  { id: 'ARC-MM-0035', name: '电白水东河治理工程检测报告', cabinetId: 'C-03', category: '水利工程', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-MM-0036', name: '化州陵江流域水土保持检测报告', cabinetId: 'C-03', category: '水利工程', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-MM-0037', name: '茂名市防洪排涝工程检测报告', cabinetId: 'C-04', category: '水利工程', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-MM-0038', name: '信宜黄华江堤防检测报告', cabinetId: 'C-04', category: '水利工程', year: '2026', boxNumber: '第7盒', status: 'normal' },
  { id: 'ARC-MM-0039', name: '高州灌区节水改造工程检测报告', cabinetId: 'C-05', category: '水利工程', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-MM-0040', name: '茂名市供水管道检测报告', cabinetId: 'C-05', category: '水利工程', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-MM-0041', name: '电白海堤加固工程检测报告', cabinetId: 'C-06', category: '水利工程', year: '2026', boxNumber: '第3盒', status: 'borrowed' },
  { id: 'ARC-MM-0042', name: '茂名市排涝泵站检测报告', cabinetId: 'C-06', category: '水利工程', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-MM-0043', name: '化州水利工程输水渠道检测报告', cabinetId: 'C-07', category: '水利工程', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-MM-0044', name: '茂名滨海新区排水工程检测报告', cabinetId: 'C-07', category: '水利工程', year: '2026', boxNumber: '第6盒', status: 'normal' },
]

// ========== 3 号档案室 — 信宜总部二楼综合档案室 ==========

const room3Zones: ZoneConfig[] = [
  { zone: 'A', startX: 120, startY: 70,  label: 'A 区 · 综合管理档案',  color: '#1677ff' },
  { zone: 'B', startX: 560, startY: 70,  label: 'B 区 · 设备与人员档案', color: '#faad14' },
]

const room3Files: ArchiveFile[] = [
  { id: 'ARC-ZH-0001', name: '集信国控2026年度经营计划文件', cabinetId: 'A-01', category: '综合管理', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-ZH-0002', name: '公司年度工作报告及会议纪要', cabinetId: 'A-01', category: '综合管理', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-ZH-0003', name: '检测业务合同及委托协议汇编', cabinetId: 'A-02', category: '综合管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-ZH-0004', name: '客户档案及服务记录', cabinetId: 'A-02', category: '综合管理', year: '2026', boxNumber: '第5盒', status: 'borrowed' },
  { id: 'ARC-ZH-0005', name: '2025年度财务审计报告', cabinetId: 'A-03', category: '综合管理', year: '2025', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-ZH-0006', name: '税务申报及发票存根', cabinetId: 'A-03', category: '综合管理', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-ZH-0007', name: '公司管理制度及操作规程汇编', cabinetId: 'A-04', category: '综合管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-ZH-0008', name: '法律事务及合同审查记录', cabinetId: 'A-04', category: '综合管理', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-ZH-0009', name: '党建工作文件及会议记录', cabinetId: 'A-05', category: '综合管理', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-ZH-0010', name: '工会活动及员工福利记录', cabinetId: 'A-05', category: '综合管理', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-ZH-0011', name: '信息化建设项目文件', cabinetId: 'A-06', category: '综合管理', year: '2026', boxNumber: '第4盒', status: 'borrowed' },
  { id: 'ARC-ZH-0012', name: ' OA系统需求及设计文档', cabinetId: 'A-06', category: '综合管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-ZH-0013', name: '检测报告质量抽查及整改记录', cabinetId: 'A-07', category: '综合管理', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-ZH-0014', name: '能力验证及实验室比对结果', cabinetId: 'A-07', category: '综合管理', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-ZH-0015', name: '供应商评价及合格供方名录', cabinetId: 'A-08', category: '综合管理', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-ZH-0016', name: '采购合同及验收记录汇编', cabinetId: 'A-08', category: '综合管理', year: '2026', boxNumber: '第4盒', status: 'normal' },
  { id: 'ARC-ZH-0017', name: '万能试验机校准证书及记录', cabinetId: 'B-01', category: '设备管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-ZH-0018', name: '压力机年度检定报告', cabinetId: 'B-01', category: '设备管理', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-ZH-0019', name: '混凝土养护室设备维护记录', cabinetId: 'B-02', category: '设备管理', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-ZH-0020', name: '沥青混合料搅拌机验收报告', cabinetId: 'B-02', category: '设备管理', year: '2026', boxNumber: '第4盒', status: 'borrowed' },
  { id: 'ARC-ZH-0021', name: '检测人员上岗证及资质证书', cabinetId: 'B-03', category: '人员管理', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-ZH-0022', name: '技术人员职称评审材料', cabinetId: 'B-03', category: '人员管理', year: '2026', boxNumber: '第6盒', status: 'normal' },
  { id: 'ARC-ZH-0023', name: '员工培训计划及考核记录', cabinetId: 'B-04', category: '人员管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-ZH-0024', name: '安全生产培训及考试记录', cabinetId: 'B-04', category: '人员管理', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-ZH-0025', name: '汽车检测线设备运行记录', cabinetId: 'B-05', category: '设备管理', year: '2026', boxNumber: '第1盒', status: 'normal' },
  { id: 'ARC-ZH-0026', name: '无损检测设备校准记录', cabinetId: 'B-05', category: '设备管理', year: '2026', boxNumber: '第4盒', status: 'borrowed' },
  { id: 'ARC-ZH-0027', name: '化学分析天平检定证书', cabinetId: 'B-06', category: '设备管理', year: '2026', boxNumber: '第3盒', status: 'normal' },
  { id: 'ARC-ZH-0028', name: '员工健康档案及体检记录', cabinetId: 'B-06', category: '人员管理', year: '2026', boxNumber: '第2盒', status: 'normal' },
  { id: 'ARC-ZH-0029', name: '设备购置申请及审批文件', cabinetId: 'B-07', category: '设备管理', year: '2026', boxNumber: '第5盒', status: 'normal' },
  { id: 'ARC-ZH-0030', name: '设备报废及处置记录', cabinetId: 'B-07', category: '设备管理', year: '2026', boxNumber: '第1盒', status: 'normal' },
]

// ========== 导出档案室列表 ==========

export const rooms: ArchiveRoom[] = [
  {
    id: 'room1',
    name: '1号档案室 · 主档案室',
    location: '信宜总部 · 一楼',
    description: '材料检测 / 结构监测 / 路桥水利 / 基础检测',
    zoneConfigs: room1Zones,
    cabinets: generateCabinetsForZones(room1Zones, 'room1'),
    files: room1Files,
  },
  {
    id: 'room2',
    name: '2号档案室 · 工程档案室',
    location: '茂名分部 · 二楼',
    description: '建筑工程 / 公路水运 / 水利工程',
    zoneConfigs: room2Zones,
    cabinets: generateCabinetsForZones(room2Zones, 'room2'),
    files: room2Files,
  },
  {
    id: 'room3',
    name: '3号档案室 · 综合档案室',
    location: '信宜总部 · 二楼',
    description: '综合管理 / 设备与人员',
    zoneConfigs: room3Zones,
    cabinets: generateCabinetsForZones(room3Zones, 'room3'),
    files: room3Files,
  },
]

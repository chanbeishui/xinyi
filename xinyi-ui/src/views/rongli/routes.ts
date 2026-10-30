/**
 * 荣利后台路由路径集中定义。
 * 菜单由 sql/migration_20261022_01_rongli_menu_groups.sql 生成，分四个分组：
 * business 业务处理 / people 资料与人员 / inspect 检测与配置 / runtime 运行与审计。
 * 页面内跳转一律引用这里，避免各页各写一套路径导致 404。
 */
export const RL = {
  dashboard: '/rongli/business/dashboard',
  order: '/rongli/business/order',
  dispatch: '/rongli/business/dispatch',
  aftersale: '/rongli/business/aftersale',
  exception: '/rongli/business/exception',
  dispatchRecord: '/rongli/business/dispatchRecord',

  vehicle: '/rongli/people/vehicle',
  owner: '/rongli/people/owner',
  driver: '/rongli/people/driver',
  statement: '/rongli/people/statement',
  training: '/rongli/people/training',
  penalty: '/rongli/people/penalty',
  paymentVerify: '/rongli/people/paymentVerify',
  adjustment: '/rongli/people/adjustment',
  reconcileSummary: '/rongli/people/reconcileSummary',
  feeRule: '/rongli/people/feeRule',
  qualification: '/rongli/people/qualification',

  receivePicked: '/rongli/inspect/receivePicked',
  inspectionTodo: '/rongli/inspect/inspectionTodo',
  station: '/rongli/inspect/station',
  price: '/rongli/inspect/price',
  stationGrant: '/rongli/inspect/stationGrant',
  config: '/rongli/inspect/config',
  license: '/rongli/inspect/license',
  resultRegister: '/rongli/inspect/resultRegister',
  vehicleType: '/rongli/inspect/vehicleType',

  notification: '/rongli/runtime/notification',
  statistics: '/rongli/runtime/statistics',
  paymentReconcile: '/rongli/runtime/paymentReconcile',
  audit: '/rongli/runtime/audit',
  governance: '/rongli/runtime/governance'
}

<template>
  <ConfigShell
    kind="PAYMENT_POLICY"
    title="支付方式设置（B46）"
    subtitle="线下支付与微信支付是两个独立开关，不是单选。两者都关闭表示暂停新付费预约；历史订单查询、退款、回调不受影响。线下支付按“客户选择 → 后台登记真实收款 → 另一位有权人员独立核实全额”处理，不是先服务后结算。"
    :fields="fields"
    :secret-slots="secretSlots"
    :defaults="defaults" />
</template>

<script setup lang="ts">
import ConfigShell from './components/ConfigShell.vue'

const fields = [
  { key: 'offlineEnabled', label: '线下支付', type: 'switch' },
  { key: 'wechatEnabled', label: '微信支付', type: 'switch' },
  { key: 'wechatMerchant', label: '关联微信商户', type: 'ref', refKind: 'WECHAT_PAY',
    hint: '只能选择已发布的微信支付商户配置，且必须同主体同环境' },
  { key: 'offlineCollectionMode', label: '线下收款模式', type: 'select',
    options: [{ value: 'PREPAID_MANUAL_VERIFIED', label: '先收款，后台登记并独立核实（本版唯一支持）' }],
    hint: '本版不支持服务后结算、代驾个人代收或自动打款' },
  { key: 'offlineMethods', label: '支持的线下方式', type: 'checks',
    options: [{ value: 'CASH', label: '现金（限公司柜台）' }, { value: 'BANK_TRANSFER', label: '对公转账' }] },
  { key: 'offlineInstructions', label: '收款说明', type: 'textarea', rows: 3, max: 1000,
    hint: '展示在车主支付页，说明支付时效、到账后如何确认' },
  { key: 'offlineReceiverName', label: '收款主体名称', hint: '如：XX机动车检测有限公司' },
  { key: 'offlineAccountName', label: '对公户名', hint: '启用转账时必填，与银行开户名一致' },
  { key: 'offlineBankName', label: '开户行', hint: '启用转账时必填' },
  { key: 'offlineContactName', label: '联系人', hint: '对公业务联系人，不使用员工私人号码' },
  { key: 'offlineContactPhone', label: '联系电话', hint: '企业对外服务电话，会展示给客户' }
]

const secretSlots = [
  { key: 'offlineBankAccount', label: '对公账号', hint: '敏感内容，仅核实与对账时受控展示，页面与接口不回显明文' }
]

const defaults = {
  offlineEnabled: false,
  wechatEnabled: false,
  offlineCollectionMode: 'PREPAID_MANUAL_VERIFIED',
  offlineMethods: [],
  offlineInstructions: '',
  offlineReceiverName: '',
  offlineAccountName: '',
  offlineBankName: '',
  offlineContactName: '',
  offlineContactPhone: ''
}
</script>

<template>
  <ConfigShell
    kind="WECHAT_PAY"
    title="微信支付商户（B47）"
    subtitle="普通直连商户 + 小程序 JSAPI。新接入默认使用“微信支付公钥”验签；平台证书模式仅用于确有存量资格的商户。商户号或 AppID 一旦被交易引用就不再覆盖，换商户请新增配置身份并按新业务切换。"
    :fields="fields"
    :secret-slots="secretSlots"
    :defaults="defaults" />
</template>

<script setup lang="ts">
import ConfigShell from './components/ConfigShell.vue'

const fields = [
  { key: 'merchantName', label: '商户名称', hint: '与微信商户平台一致，用于后台核对与公开收款说明' },
  { key: 'mchid', label: '商户号 mchid', max: 32, hint: '8~12 位数字，按字符串保存，不做数值转换' },
  { key: 'appProfile', label: '小程序 AppID', type: 'ref', refKind: 'WECHAT_APP',
    hint: '从已登记的小程序公共凭据中选择；不能拿另一个 AppID 的 OpenID 发起支付' },
  { key: 'merchantCertificateSerial', label: '商户证书序列号', max: 128, hint: '与商户签名私钥配套，16~64 位十六进制' },
  { key: 'verificationMode', label: '验签方式', type: 'select',
    options: [
      { value: 'WECHATPAY_PUBLIC_KEY', label: '微信支付公钥（新接入默认）' },
      { value: 'PLATFORM_CERT', label: '平台证书（存量兼容）' }
    ] },
  { key: 'wechatPayPublicKeyId', label: '公钥 ID', max: 128, hint: '公钥模式必填，必须与上传的公钥配对' },
  { key: 'wechatPayPublicKeyPem', label: '公钥 PEM', type: 'textarea', rows: 4, max: 16384,
    hint: '从微信商户平台获取，不要从回调里给出的链接下载' },
  { key: 'merchantCertificatePem', label: '商户证书 PEM', type: 'textarea', rows: 4, max: 32768,
    hint: '可选，用于校验序列号与私钥是否匹配；私钥本身在下方凭据区单独提交' },
  { key: 'bindingAcknowledged', label: '已确认商户绑定', type: 'switch',
    hint: '在微信商户平台确认该 AppID 与商户号已绑定后才勾选' }
]

const secretSlots = [
  { key: 'merchantPrivateKey', label: '商户签名私钥', hint: 'PKCS#8 PEM，仅服务端使用' },
  { key: 'apiV3Key', label: 'APIv3 密钥', hint: '固定 32 字节，不能用 AppSecret 代替' }
]

const defaults = {
  merchantName: '',
  mchid: '',
  merchantCertificateSerial: '',
  verificationMode: 'WECHATPAY_PUBLIC_KEY',
  wechatPayPublicKeyId: '',
  wechatPayPublicKeyPem: '',
  merchantCertificatePem: '',
  bindingAcknowledged: false
}
</script>

<template>
  <ConfigShell
    kind="WECHAT_APP"
    title="小程序公共凭据（B53）"
    subtitle="同一小程序的服务端身份能力与订阅消息共用这一份 AppID/AppSecret，access_token 按应用+环境+凭据版本统一缓存，不允许各自另建缓存。AppSecret 不是商户 APIv3 密钥；根加密密钥由部署环境提供，不在此页填写。"
    :fields="fields"
    :secret-slots="secretSlots"
    :defaults="defaults" />
</template>

<script setup lang="ts">
import ConfigShell from './components/ConfigShell.vue'

const fields = [
  { key: 'enabled', label: '启用该应用', type: 'switch' },
  { key: 'appId', label: '小程序 AppID', max: 64, hint: 'wx + 16 位十六进制；被账号或交易引用后不能直接替换成另一个应用' },
  { key: 'appName', label: '小程序名称', max: 100 }
]

const secretSlots = [{ key: 'appSecret', label: 'AppSecret', hint: '仅服务端使用；用于换取 access_token，不用于支付签名' }]

const defaults = { enabled: false, appId: '', appName: '' }
</script>

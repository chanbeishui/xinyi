<template>
  <div>
    <el-tabs v-model="tab" type="border-card">
      <el-tab-pane label="腾讯云短信（B48）" name="tencent">
        <ConfigShell
          v-if="tab === 'tencent'"
          kind="TENCENT_SMS"
          title="腾讯云短信通道"
          subtitle="接口版本与接入点由服务端固定，不接受任意 URL。参数按 TemplateParamSet 的位置顺序渲染；ACCEPTED 只代表受理，不等于送达。超时记为 UNKNOWN 并查询回执，不自动切到另一个通道重发。"
          :fields="tencentFields"
          :secret-slots="tencentSecrets"
          :defaults="tencentDefaults" />
      </el-tab-pane>
      <el-tab-pane label="阿里云短信（B48）" name="aliyun">
        <ConfigShell
          v-if="tab === 'aliyun'"
          kind="ALIYUN_SMS"
          title="阿里云短信通道"
          subtitle="接口版本 2017-05-25 固定；TemplateParam 由服务端编码为 JSON 字符串，管理员只维护变量映射，不拼接 HTTP。阿里云 SendSms 没有供应商幂等保证，因此本系统不宣称外部严格只发一次。"
          :fields="aliyunFields"
          :secret-slots="aliyunSecrets"
          :defaults="aliyunDefaults" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ConfigShell from './components/ConfigShell.vue'

const tab = ref('tencent')

const tencentFields = [
  { key: 'enabled', label: '启用该通道', type: 'switch' },
  { key: 'apiVersion', label: '接口版本', type: 'select',
    options: [{ value: '2021-01-11', label: '2021-01-11（固定）' }] },
  { key: 'region', label: 'Region', max: 64, hint: '如 ap-guangzhou' },
  { key: 'smsSdkAppId', label: 'SdkAppId', max: 32, hint: '短信应用 ID，不是 SecretId' },
  { key: 'secretId', label: 'SecretId', max: 128 },
  { key: 'signName', label: '短信签名内容', max: 100, hint: '填写签名内容（如“荣利检测”），不是签名 ID' }
]

const tencentSecrets = [{ key: 'secretKey', label: 'SecretKey', hint: '仅服务端使用，保存后不再回显' }]

const tencentDefaults = {
  enabled: false,
  apiVersion: '2021-01-11',
  region: '',
  smsSdkAppId: '',
  secretId: '',
  signName: ''
}

const aliyunFields = [
  { key: 'enabled', label: '启用该通道', type: 'switch' },
  { key: 'apiVersion', label: '接口版本', type: 'select',
    options: [{ value: '2017-05-25', label: '2017-05-25（固定）' }] },
  { key: 'region', label: 'RegionId', max: 64, hint: '默认 cn-hangzhou' },
  { key: 'accessKeyId', label: 'AccessKeyId', max: 128 },
  { key: 'signName', label: '短信签名内容', max: 100, hint: '填写签名内容，不是签名 ID' }
]

const aliyunSecrets = [{ key: 'accessKeySecret', label: 'AccessKeySecret', hint: '仅服务端使用，保存后不再回显' }]

const aliyunDefaults = {
  enabled: false,
  apiVersion: '2017-05-25',
  region: 'cn-hangzhou',
  accessKeyId: '',
  signName: ''
}
</script>

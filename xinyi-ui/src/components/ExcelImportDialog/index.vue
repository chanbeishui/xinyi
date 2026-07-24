<template>
  <el-dialog :title="title" v-model="visible" :width="width" append-to-body @close="handleClose">
    <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :headers="headers" :action="uploadUrl" :disabled="isUploading" :on-progress="handleProgress" :on-change="handleFileChange" :on-remove="handleFileRemove" :on-success="handleSuccess" :auto-upload="false" drag>
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip text-center">
          <div class="el-upload__tip">
            <el-checkbox v-model="updateSupport"> {{ updateSupportLabel }} </el-checkbox>
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link v-if="templateUrl" type="primary" underline="never" style="font-size: 12px; vertical-align: baseline" @click="handleDownloadTemplate">下载模板</el-link>
        </div>
      </template>
    </el-upload>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
        <el-button @click="visible = false">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type {  AjaxResult } from '@/types/api/common'
import { getToken } from '@/utils/auth'
import request from '@/utils/request'

const { proxy } = getCurrentInstance()

const props = defineProps({
  // 对话框标题
  title: {
    type: String,
    default: '数据导入'
  },
  // 对话框宽度
  width: {
    type: String,
    default: '400px'
  },
  // 上传接口地址（必传）
  action: {
    type: String,
    required: true
  },
  // 安全导入预检接口；与executeAction同时配置时启用双阶段导入
  previewAction: {
    type: String,
    default: ''
  },
  // 安全导入正式执行接口
  executeAction: {
    type: String,
    default: ''
  },
  // 模板下载接口地址，不传则不显示下载模板链接
  templateAction: {
    type: String,
    default: ''
  },
  // 模板文件名前缀
  templateFileName: {
    type: String,
    default: 'template'
  },
  // 覆盖更新勾选框的说明文字
  updateSupportLabel: {
    type: String,
    default: '是否更新已经存在的数据'
  }
})

const emit = defineEmits(['success'])

const uploadRef = ref<any>()
const visible = ref<boolean>(false)
const selectedFile = ref<any>(null)
const isUploading = ref<boolean>(false)
const updateSupport = ref<boolean>(false)
const headers = { Authorization: 'Bearer ' + getToken() }

const uploadUrl = computed(() => {
  return import.meta.env.VITE_APP_BASE_API + props.action + '?updateSupport=' + (updateSupport.value ? 1 : 0)
})

const templateUrl = computed(() => !!props.templateAction)

// 打开对话框（供父组件通过 ref 调用）
function open(): void {
  updateSupport.value = false
  isUploading.value = false
  visible.value = true
  nextTick(() => {
    selectedFile.value = null
    uploadRef.value?.clearFiles()
  })
}

// 关闭时清理
function handleClose(): void {
  isUploading.value = false
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

// 下载模板
function handleDownloadTemplate(): void {
  proxy.download(props.templateAction, {}, `${props.templateFileName}_${new Date().getTime()}.xlsx`)
}

// 上传进度
function handleProgress(): void {
  isUploading.value = true
}

/** 文件选择处理 */
const handleFileChange = (file: any): void => {
  selectedFile.value = file
}

/** 文件删除处理 */
const handleFileRemove = (file: any) => {
  selectedFile.value = null
}

// 上传成功
function handleSuccess(response: AjaxResult) {
  visible.value = false
  isUploading.value = false
  selectedFile.value = null
  uploadRef.value?.clearFiles()
  proxy.$alert("<div style='overflow:auto;overflow-x:hidden;max-height:70vh;padding:10px 20px 0;'>" + response.msg + '</div>', '导入结果', { dangerouslyUseHTMLString: true })
  emit('success')
}

// 提交上传
function handleSubmit() {
  const file = selectedFile.value
  if (!file || file.length === 0 || !file.name.toLowerCase().endsWith('.xls') && !file.name.toLowerCase().endsWith('.xlsx')) {
    proxy.$modal.msgError("请选择后缀为 “xls”或“xlsx”的文件。")
    return
  }
  if (props.previewAction && props.executeAction) {
    handleSecureSubmit(file.raw)
    return
  }
  uploadRef.value.submit()
}

async function handleSecureSubmit(file: File) {
  isUploading.value = true
  try {
    const previewForm = new FormData()
    previewForm.append('file', file)
    previewForm.append('updateSupport', String(updateSupport.value))
    const previewResponse: any = await request({
      url: props.previewAction,
      method: 'post',
      headers: { 'Content-Type': 'multipart/form-data' },
      data: previewForm
    })
    const summary = previewResponse.data
    const message = `共 ${summary.rowCount} 行：新增 ${summary.createCount}，覆盖 ${summary.updateCount}，涉及平台账号 ${summary.platformAccountCount}。请输入本次导入原因后确认执行。`
    const promptResult = await proxy.$prompt(message, '导入预检通过', {
      confirmButtonText: '确认执行',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '必须填写导入原因',
      closeOnClickModal: false
    })

    const executeForm = new FormData()
    executeForm.append('file', file)
    executeForm.append('updateSupport', String(updateSupport.value))
    executeForm.append('previewToken', summary.previewToken)
    executeForm.append('confirmed', 'true')
    executeForm.append('reason', promptResult.value)
    await request({
      url: props.executeAction,
      method: 'post',
      headers: { 'Content-Type': 'multipart/form-data' },
      data: executeForm
    })
    visible.value = false
    proxy.$modal.msgSuccess('导入成功')
    emit('success')
  } catch (error) {
    // 请求错误由统一拦截器提示；取消确认无需额外提示。
  } finally {
    isUploading.value = false
  }
}

defineExpose({ open })
</script>

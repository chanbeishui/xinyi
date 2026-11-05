<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="options.img" title="点击上传头像" class="img-circle img-lg" />
    <el-dialog :title="title" v-model="open" width="800px" append-to-body @opened="modalOpened" @close="closeDialog">
      <el-row>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <vue-cropper
            ref="cropper"
            :img="cropImg"
            :info="true"
            :autoCrop="options.autoCrop"
            :autoCropWidth="options.autoCropWidth"
            :autoCropHeight="options.autoCropHeight"
            :fixedBox="options.fixedBox"
            :outputType="options.outputType"
            @realTime="realTime"
            v-if="visible"
          />
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img :src="options.previews.url" :style="options.previews.img" />
          </div>
        </el-col>
      </el-row>
      <br />
      <el-row>
        <el-col :lg="2" :md="2">
          <el-upload
            action="#"
            :http-request="requestUpload"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <el-button>
              选择
              <el-icon class="el-icon--right"><Upload /></el-icon>
            </el-button>
          </el-upload>
        </el-col>
        <el-col :lg="{ span: 1, offset: 2 }" :md="2">
          <el-button icon="Plus" @click="changeScale(1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="Minus" @click="changeScale(-1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="RefreshLeft" @click="rotateLeft()"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="RefreshRight" @click="rotateRight()"></el-button>
        </el-col>
        <el-col :lg="{ span: 2, offset: 6 }" :md="2">
          <el-button type="primary" @click="uploadImg()">提 交</el-button>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import "vue-cropper/dist/index.css"
import { VueCropper } from "vue-cropper"
import { uploadAvatar } from "@/api/system/user"
import { isExternal } from "@/utils/validate"
import useUserStore from "@/store/modules/user"

const userStore = useUserStore()
const { proxy } = getCurrentInstance()

const open = ref<boolean>(false)
const visible = ref<boolean>(false)
const title = ref<string>("修改头像")
/** 交给裁剪器的图片地址，与页面展示用的 options.img 分开 */
const cropImg = ref<string>("")

//图片裁剪数据
const options = reactive<any>({
  img: userStore.avatar,     // 裁剪图片的地址
  autoCrop: true,            // 是否默认生成截图框
  autoCropWidth: 200,        // 默认生成截图框宽度
  autoCropHeight: 200,       // 默认生成截图框高度
  fixedBox: true,            // 固定截图框大小 不允许改变
  outputType: "png",         // 默认生成截图为PNG格式
  previews: {}               //预览数据
})

/**
 * 裁剪器加载图片时会带 crossOrigin=anonymous（它要读 canvas 像素才能导出截图），
 * 而 OSS 直链没有返回 Access-Control-Allow-Origin，跨域头像在裁剪区会加载失败：
 * 左边一片空白、右边预览也是空的，但页面上那张 <img> 却能正常显示（它不需要 CORS）。
 *
 * 所以裁剪时改用同源的 /profile 兼容层取同一个对象（后端从 OSS 读流返回），
 * 绕开 CORS 又不用改桶配置。页面展示仍然用 OSS 直链。
 */
function sameOriginImage(url: string): string {
  if (!url || !isExternal(url)) return url
  try {
    const key = new URL(url).pathname.replace(/^\/+/, "")
    return key ? `${import.meta.env.VITE_APP_BASE_API}/profile/${key}` : url
  } catch {
    return url
  }
}

/**
 * 裁剪器输出固定是 PNG（options.outputType），但 FormData 里若沿用用户选的原始文件名，
 * 就会出现「PNG 字节 + .jpg 后缀」；后端按魔数核对后判为名实不符直接拒收（提示还是「后缀不正确」），
 * 看起来像用户传错了格式。这里按 Blob 的真实类型重新命名。
 */
function avatarFileName(blob: Blob): string {
  switch ((blob.type || "").toLowerCase()) {
    case "image/jpeg":
      return "avatar.jpg"
    case "image/gif":
      return "avatar.gif"
    case "image/bmp":
      return "avatar.bmp"
    default:
      return "avatar.png"
  }
}

/** 编辑头像 */
function editCropper() {
  cropImg.value = sameOriginImage(userStore.avatar)
  open.value = true
}

/** 打开弹出层结束时的回调 */
function modalOpened() {
  visible.value = true
}

/** 覆盖默认上传行为 */
function requestUpload() {}

/** 向左旋转 */
function rotateLeft() {
  proxy.$refs.cropper.rotateLeft()
}

/** 向右旋转 */
function rotateRight() {
  proxy.$refs.cropper.rotateRight()
}

/** 图片缩放 */
function changeScale(num: number) {
  num = num || 1
  proxy.$refs.cropper.changeScale(num)
}

/** 上传预处理 */
function beforeUpload(file: File) {
  if (file.type.indexOf("image/") == -1) {
    proxy.$modal.msgError("文件格式错误，请上传图片类型,如：JPG，PNG后缀的文件。")
  } else {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => {
      cropImg.value = String(reader.result || "")
    }
  }
}

/** 上传图片 */
function uploadImg() {
  proxy.$refs.cropper.getCropBlob((data: Blob) => {
    const formData = new FormData()
    formData.append("avatarfile", data, avatarFileName(data))
    uploadAvatar(formData).then(response => {
      open.value = false
      // 后端返回的 imgUrl 改走 OSS 后是完整直链，只有历史的 /profile/... 相对地址才需要拼接口前缀。
      // 无条件拼会得到 /dev-apihttps://... 这种非法地址：上传当次头像裂图，刷新后 getInfo()
      // 走 isHttp 分支直接用直链才恢复正常，很容易误判成 OSS 配置问题。
      const imgUrl = response.imgUrl || ""
      options.img = !imgUrl ? "" : (isExternal(imgUrl) ? imgUrl : import.meta.env.VITE_APP_BASE_API + imgUrl)
      userStore.avatar = options.img
      proxy.$modal.msgSuccess("修改成功")
      visible.value = false
    })
  })
}

/** 实时预览 */
function realTime(data: any) {
  options.previews = data
}

/** 关闭窗口 */
function closeDialog() {
  options.img = userStore.avatar
  cropImg.value = sameOriginImage(userStore.avatar)
}
</script>

<style lang='scss' scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
}

.user-info-head:hover:after {
  content: "+";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  cursor: pointer;
  line-height: 110px;
  border-radius: 50%;
}
</style>
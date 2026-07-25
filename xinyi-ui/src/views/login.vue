<template>
  <div class="login">
    <div class="login-aurora" aria-hidden="true"></div>
    <section class="brand-panel">
      <img src="@/assets/logo/xinyi-lotus.png" alt="集信国控" class="brand-mark" />
      <p class="brand-kicker">XINYI WORKSPACE</p>
      <h1>让每一次运营<br />都更笃定。</h1>
      <p class="brand-copy">为组织提供清晰、安全、流畅的数字工作体验。</p>
    </section>
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <div class="form-brand"><img src="@/assets/logo/xinyi-lotus.png" alt="" /><span>信仪</span></div>
      <p class="form-eyebrow">欢迎回来</p>
      <h3 class="title">{{ title }}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'
import type { CaptchaInfoResult } from '@/types/api/login'
import type { LoginForm } from '@/types/api/login'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref<LoginForm>({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref<string | undefined>(undefined)

watch(route, (newRoute: any) => {
    redirect.value = (newRoute.query && newRoute.query.redirect) as string | undefined
}, { immediate: true })

function handleLogin(): void {
  proxy.$refs.loginRef.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc: Record<string, any>, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode(): void {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie(): void {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  overflow: hidden;
  background: linear-gradient(160deg, #eef4ff 0%, #f8fbff 45%, #ffffff 100%);
  position: relative;
  gap: clamp(48px, 12vw, 190px);
}
.login-aurora {
  position: absolute;
  width: 62vw;
  height: 62vw;
  border-radius: 50%;
  left: -22vw;
  top: -30vw;
  background: radial-gradient(circle, rgba(22,119,255,.16), rgba(22,119,255,.05) 42%, transparent 68%);
  filter: blur(10px);
  &::after {
    content: '';
    position: absolute;
    right: -46vw;
    bottom: -56vw;
    width: 40vw;
    height: 40vw;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(68,184,153,.12), transparent 66%);
  }
}
.brand-panel { position: relative; color: #101828; width: 360px; z-index: 1; }
.brand-mark {
  width: 60px;
  height: 60px;
  margin-bottom: 36px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 14px 30px rgba(22,119,255,.14), 0 0 0 1px rgba(16,24,40,.05);
}
.brand-kicker, .form-eyebrow { margin: 0 0 12px; color: #1677ff; font-size: 11px; font-weight: 700; letter-spacing: .16em; }
.brand-panel h1 { margin: 0; font-size: 42px; line-height: 1.22; letter-spacing: -.05em; font-weight: 650; color: #101828; }
.brand-copy { width: 270px; margin-top: 22px; color: #667085; font-size: 14px; line-height: 1.8; }
.title {
  margin: 0 0 32px;
  color: #182230;
  font-size: 25px;
  letter-spacing: -.04em;
}

.login-form {
  border-radius: 22px;
  background: rgba(255,255,255,.92);
  width: 390px;
  padding: 32px 32px 12px;
  z-index: 1;
  border: 1px solid rgba(16,24,40,.06);
  box-shadow: 0 24px 64px rgba(22,60,140,.12);
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}
.form-brand { display:flex; align-items:center; gap:9px; color:#101828; font-size:17px; font-weight:700; margin-bottom:35px; letter-spacing:.08em; }
.form-brand img { width:28px; height:28px; border-radius: 8px; }
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #98a2b3;
}
.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #98a2b3;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
@media (max-width: 760px) { .brand-panel { display:none; } .login { padding:24px; } .login-form { width:min(390px, 100%); } }
.login-code-img {
  height: 40px;
  padding-left: 12px;
}

html.dark .login {
  background: linear-gradient(160deg, #0b1526 0%, #101a2e 55%, #0d1420 100%);
  .login-aurora { background: radial-gradient(circle, rgba(45,110,255,.28), rgba(45,110,255,.08) 42%, transparent 68%); }
  .brand-panel { color: #f2f4f7; }
  .brand-panel h1 { color: #f2f4f7; }
  .brand-copy { color: #98a2b3; }
  .brand-mark { background: #1b2432; box-shadow: 0 14px 30px rgba(0,0,0,.4); }
  .title, .form-brand { color: #f2f4f7; }
  .login-form {
    background: rgba(27,36,50,.92) !important;
    border-color: rgba(255,255,255,.08);
    box-shadow: 0 24px 64px rgba(0,0,0,.45);
  }
}
</style>

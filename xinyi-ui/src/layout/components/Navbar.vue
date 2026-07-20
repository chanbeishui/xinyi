<template>
  <div class="navbar" :class="'nav' + settingsStore.navType">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb v-if="settingsStore.navType == 1" id="breadcrumb-container" class="breadcrumb-container" />
    <top-nav v-if="settingsStore.navType == 2" id="topmenu-container" class="topmenu-container" />
    <template v-if="settingsStore.navType == 3">
      <logo v-show="settingsStore.sidebarLogo" :collapse="false" />
      <top-bar id="topbar-container" class="topbar-container" />
    </template>

    <div class="right-menu">
      <header-notice v-if="appStore.device !== 'mobile'" id="header-notice" class="right-menu-item hover-effect" />
      <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="hover" @command="handleCommand">
        <div class="avatar-wrapper">
          <img :src="userStore.avatar" class="user-avatar" />
          <span class="user-nickname">{{ userStore.nickName }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/user/profile"><el-dropdown-item>个人中心</el-dropdown-item></router-link>
            <el-dropdown-item v-if="settingsStore.showSettings" command="setLayout">布局设置</el-dropdown-item>
            <el-dropdown-item command="lockScreen">锁定屏幕</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb/index.vue'
import TopNav from './TopNav/index.vue'
import TopBar from './TopBar/index.vue'
import Logo from './Sidebar/Logo.vue'
import Hamburger from '@/components/Hamburger/index.vue'
import HeaderNotice from './HeaderNotice/index.vue'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const lockStore = useLockStore()
const settingsStore = useSettingsStore()
const emits = defineEmits(['setLayout'])

function toggleSideBar() { appStore.toggleSideBar() }
function handleCommand(command: string) {
  if (command === 'setLayout') emits('setLayout')
  if (command === 'lockScreen') { lockStore.lockScreen(route.fullPath); router.push('/lock') }
  if (command === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
      .then(() => userStore.logOut().then(() => { location.href = '/index' }))
      .catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.navbar { height: 50px; position: relative; display:flex; align-items:center; background:var(--navbar-bg); box-shadow:0 1px 4px rgba(0,21,41,.08); }
.navbar.nav3 .hamburger-container { display:none !important; }
.hamburger-container { height:100%; display:flex; align-items:center; margin-right:8px; cursor:pointer; }
.breadcrumb-container { flex-shrink:0; }
.topmenu-container { position:absolute; left:50px; }
.topbar-container { flex:1; min-width:0; margin-left:8px; }
.right-menu { height:100%; margin-left:auto; display:flex; align-items:center; }
.right-menu-item { height:100%; padding:0 10px; color:#5a6472; display:flex; align-items:center; }
.hover-effect { cursor:pointer; transition:background .2s; }
.hover-effect:hover { background:rgba(0,0,0,.025); }
.avatar-container { padding-right:14px; }
.avatar-wrapper { display:flex; align-items:center; gap:8px; }
.user-avatar { width:30px; height:30px; border-radius:50%; object-fit:cover; }
.user-nickname { font-size:14px; font-weight:600; }
</style>

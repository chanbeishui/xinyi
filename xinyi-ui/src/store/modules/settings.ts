import defaultSettings from '@/settings'
import { useColorMode, usePreferredDark } from '@vueuse/core'
import { useDynamicTitle } from '@/utils/dynamicTitle'
import { handleThemeStyle } from '@/utils/theme'

const colorMode = useColorMode({
  storageKey: 'vueuse-color-scheme',
  storage: localStorage,
})

const prefersDark = usePreferredDark()

// 响应式计算当前是否为深色模式
const isDarkComputed = computed(() => {
  if (colorMode.value === 'auto') return prefersDark.value
  return colorMode.value === 'dark'
})

const { sideTheme, showSettings, navType, tagsView, tagsViewPersist, tagsIcon, tagsViewStyle, fixedHeader, sidebarLogo, dynamicTitle, footerVisible, footerContent, colorScheme } = defaultSettings

const storageSetting = JSON.parse(localStorage.getItem('layout-setting') || '{}') || {}

interface SettingsState {
  title: string
  theme: string
  sideTheme: string
  showSettings: boolean
  navType: number
  tagsView: boolean
  tagsViewPersist: boolean
  tagsViewStyle: string
  tagsIcon: boolean
  fixedHeader: boolean
  sidebarLogo: boolean
  dynamicTitle: boolean
  footerVisible: boolean
  footerContent: string
  colorScheme: string
}

const useSettingsStore = defineStore(
  'settings',
  {
    state: (): SettingsState => ({
      title: '',
      theme: storageSetting.theme || '#1677ff',
      sideTheme: storageSetting.sideTheme || sideTheme,
      showSettings: showSettings,
      navType: storageSetting.navType === undefined ? navType : storageSetting.navType,
      tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
      tagsViewPersist: storageSetting.tagsViewPersist === undefined ? tagsViewPersist : storageSetting.tagsViewPersist,
      tagsIcon: storageSetting.tagsIcon === undefined ? tagsIcon : storageSetting.tagsIcon,
      tagsViewStyle: storageSetting.tagsViewStyle === undefined ? tagsViewStyle : storageSetting.tagsViewStyle,
      fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
      sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
      dynamicTitle: storageSetting.dynamicTitle === undefined ? dynamicTitle : storageSetting.dynamicTitle,
      footerVisible: storageSetting.footerVisible === undefined ? footerVisible : storageSetting.footerVisible,
      footerContent: footerContent,
      colorScheme: storageSetting.colorScheme || colorScheme || 'auto'
    }),
    getters: {
      isDark(): boolean {
        return isDarkComputed.value
      }
    },
    actions: {
      // 修改布局设置
      changeSetting(data: { key: string; value: any }) {
        const { key, value } = data
        if (this.hasOwnProperty(key)) {
          (this as any)[key] = value
        }
      },
      // 设置网页标题
      setTitle(title: string) {
        this.title = title
        useDynamicTitle()
      },
      // 设置外观模式：light / dark / auto
      setColorScheme(mode: string) {
        this.colorScheme = mode
        colorMode.value = mode
        nextTick(() => {
          handleThemeStyle(this.theme)
        })
      }
    }
  })

export default useSettingsStore

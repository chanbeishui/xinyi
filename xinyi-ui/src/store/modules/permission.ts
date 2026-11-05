import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index.vue'
import ParentView from '@/components/ParentView/index.vue'
import InnerLink from '@/layout/components/InnerLink/index.vue'

// 匹配views里面所有的.vue文件
const modules = import.meta.glob('./../../views/**/*.vue')

const usePermissionStore = defineStore(
  'permission',
  {
    state: () => ({
      routes: [] as any[],
      addRoutes: [] as any[],
      defaultRoutes: [] as any[],
      topbarRouters: [] as any[],
      sidebarRouters: [] as any[]
    }),
    actions: {
      setRoutes(routes: any[]) {
        this.addRoutes = routes
        this.routes = constantRoutes.concat(routes)
      },
      setDefaultRoutes(routes: any[]) {
        this.defaultRoutes = constantRoutes.concat(routes)
      },
      setTopbarRoutes(routes: any[]) {
        this.topbarRouters = routes
      },
      setSidebarRouters(routes: any[]) {
        this.sidebarRouters = routes
      },
      generateRoutes(roles?: any[]): Promise<any[]> {
        return new Promise(resolve => {
          // 向后端请求路由数据
          getRouters().then(res => {
            const sdata = JSON.parse(JSON.stringify(res.data))
            const rdata = JSON.parse(JSON.stringify(res.data))
            const defaultData = JSON.parse(JSON.stringify(res.data))
            // 必须在 filterAsyncRouter 之前做：那时三份数据结构完全一致，改出来的名字才一致
            dedupeRouteNames(sdata)
            dedupeRouteNames(rdata)
            dedupeRouteNames(defaultData)
            const sidebarRoutes = filterAsyncRouter(sdata)
            const rewriteRoutes = filterAsyncRouter(rdata, false, true)
            const defaultRoutes = filterAsyncRouter(defaultData)
            const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
            asyncRoutes.forEach(route => { router.addRoute(route) })
            this.setRoutes(rewriteRoutes)
            this.setSidebarRouters(constantRoutes.concat(sidebarRoutes))
            this.setDefaultRoutes(sidebarRoutes)
            this.setTopbarRoutes(defaultRoutes)
            resolve(rewriteRoutes)
          })
        })
      }
    }
  })

/**
 * 保证动态路由名全局唯一。
 *
 * Vue Router 4 的 router.addRoute() 遇到已存在的同名路由会先移除旧记录，
 * 后注册的页面于是把先注册的页面顶掉，表现就是「菜单点得动，进去是 404」。
 * 后端只在 route_name 为空时才用 path 首字母大写兜底，而 path 只在同一父级下唯一，
 * 跨层级撞名很常见（system/config 与 rongli/inspect/config 都叫 Config，
 * rongli/inspect/config/wechatPay 与 rongli/integration/wechatPay 都叫 WechatPay）。
 * 静态路由已占用的名字同样要避让。
 *
 * 三份路由数据（sidebar / rewrite / default）在调用本函数时结构完全一致，
 * 各自独立去重可以得到一致的命名，不会出现「路由表里叫 A、侧边栏/tab 里叫 B」的错位。
 */
function dedupeRouteNames(routes: any[]) {
  const used = new Set<string>()
  constantRoutes.forEach((route: any) => {
    if (route.name) used.add(String(route.name))
  })
  const walk = (list: any[]) => {
    list.forEach(route => {
      if (route.name) {
        const origin = String(route.name)
        let name = origin
        let index = 2
        while (used.has(name)) name = `${origin}_${index++}`
        used.add(name)
        if (name !== origin) {
          console.warn(`[router] 路由名 ${origin} 重复，已改用 ${name}（path: ${route.path}）`)
          route.name = name
        }
      }
      if (route.children && route.children.length) walk(route.children)
    })
  }
  walk(routes)
}

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap: any[], lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap: any[], lastRouter: any = false) {
  var children: any[] = []
  childrenMap.forEach(el => {
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes: any[]): any[] {
  const res: any[] = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view: string): any => {
  let res
  for (const path in modules) {
    const dir = path.split('views/')[1].split('.vue')[0]
    if (dir === view) {
      res = () => modules[path]()
    }
  }
  return res
}

export default usePermissionStore

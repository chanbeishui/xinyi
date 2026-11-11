import { amapErrorMessage, loadAMap } from './amap'

/**
 * 省 / 市 / 区三级行政区划 + 地址拼接。
 *
 * 数据不从仓库里带静态表：行政区划每年都在变（撤县设区、代管调整），把两三千条
 * 名字塞进仓库既占体积又很快过期。这里用高德的 DistrictSearch 一次取全国三级树，
 * 按「省 → 市 → 区」压成 { 省: { 市: [区…] } } 存 localStorage，30 天有效。
 * 取不到（未配 Key / 网络失败）时由调用方降级为单行文本地址，不阻塞录入。
 */
export type Region = { name: string; children?: Region[] }

export type RegionParts = { province: string; city: string; district: string; detail: string }

type RegionTree = Record<string, Record<string, string[]>>

const CACHE_KEY = 'rongli.region.tree.v2'
const CACHE_TTL = 30 * 24 * 60 * 60 * 1000

/** 从一整串地址里「切」出行政区名字，比 startsWith 可靠：
 *  "吉林市" 不会因为前缀是 "吉林" 就被当成 "吉林省" */
const PROVINCE_TOKEN = /^(.{2,}?(?:省|自治区|特别行政区))/
const CITY_TOKEN = /^(.{2,}?(?:市|自治州|地区|盟))/
const DISTRICT_TOKEN = /^(.{2,}?(?:区|县|市|旗))/

/** 省级/市级/区级后缀，用于「广东省」与「广东」互认（只用于拼接去重，不用于拆分） */
const REGION_SUFFIX = /(特别行政区|自治区|自治州|地区|新区|省|市|盟|区|县|旗)$/

let cache: Region[] | null = null
let task: Promise<Region[]> | null = null

function stripSuffix(name: string): string {
  const short = String(name || '').replace(REGION_SUFFIX, '')
  return short.length >= 2 ? short : ''
}

function nameForms(name: string): string[] {
  const full = String(name || '').trim()
  if (!full) return []
  const short = stripSuffix(full)
  return short && short !== full ? [full, short] : [full]
}

function readCache(): Region[] | null {
  if (cache) return cache
  try {
    const raw = localStorage.getItem(CACHE_KEY)
    if (!raw) return null
    const saved = JSON.parse(raw)
    if (!saved || !Array.isArray(saved.tree) || Date.now() - Number(saved.ts || 0) > CACHE_TTL) return null
    cache = saved.tree as Region[]
    return cache
  } catch {
    return null
  }
}

function writeCache(tree: Region[]) {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify({ ts: Date.now(), tree }))
  } catch {
    // 隐私模式或配额满时忽略：下次进来重新拉一次即可
  }
}

function toRegions(tree: RegionTree): Region[] {
  return Object.keys(tree).map((province) => {
    const cities = tree[province] || {}
    return {
      name: province,
      children: Object.keys(cities).map((city) => ({
        name: city,
        children: (cities[city] || []).map((district) => ({ name: district }))
      }))
    }
  })
}

async function fetchTree(): Promise<Region[]> {
  const AMap = await loadAMap()
  const country = await new Promise<any>((resolve, reject) => {
    const search = new AMap.DistrictSearch({ level: 'country', subdistrict: 3, extensions: 'base' })
    search.search('中国', (status: string, result: any) => {
      const root = result && result.districtList && result.districtList[0]
      if (status === 'complete' && root) return resolve(root)
      // 额度用尽 / Key 无效这类错误要原样报出去，别兜成「加载失败」
      reject(new Error(status === 'error' ? amapErrorMessage(result) : '行政区划数据获取失败'))
    })
  })

  const tree: RegionTree = {}
  for (const province of country.districtList || []) {
    const provinceName = String(province.name || '').trim()
    if (!provinceName) continue
    const cities: Record<string, string[]> = {}
    for (const city of province.districtList || []) {
      const cityName = String(city.name || '').trim()
      if (!cityName) continue
      cities[cityName] = (city.districtList || []).map((d: any) => String(d.name || '').trim()).filter(Boolean)
    }
    const hasDistricts = Object.keys(cities).some((name) => cities[name].length > 0)
    const onlyOnePlaceholder =
      Object.keys(cities).length === 1 &&
      /^(市辖区|县|省直辖县级行政区划|自治区直辖县级行政区划)$/.test(Object.keys(cities)[0]) &&
      cities[Object.keys(cities)[0]].length > 0
    // 直辖市（北京/上海/天津/重庆）高德会把区县直接挂在省级下，或者只套一层「市辖区」，
    // 这里补一层同名「市」，保证三级下拉在各地都一致，市级下拉也不会出现一堆区名
    if (onlyOnePlaceholder) {
      const placeholder = Object.keys(cities)[0]
      tree[provinceName] = { [provinceName]: cities[placeholder] }
    } else if (!hasDistricts && /市$/.test(provinceName)) {
      tree[provinceName] = { [provinceName]: Object.keys(cities) }
    } else {
      tree[provinceName] = cities
    }
  }

  const regions = toRegions(tree)
  if (!regions.length) throw new Error('行政区划数据为空')
  writeCache(regions)
  return regions
}

export function loadRegionTree(): Promise<Region[]> {
  if (task) return task
  const cached = readCache()
  if (cached) {
    task = Promise.resolve(cached)
    return task
  }
  task = fetchTree()
  // 失败不缓存，下次进抽屉还能重试
  task.catch(() => {
    task = null
  })
  return task
}

/**
 * 把「广东省茂名市茂南区站前路1号」拆回省/市/区和剩余详细地址，用于编辑时回填下拉。
 *
 * 老数据可能没写省（"茂名市茂南区…"），所以省级切不出来时用市级反查归属省。
 * 完全认不出来就把整串留在 detail 里，信息不丢。
 */
export function splitAddress(address: string, regions: Region[]): RegionParts {
  const parts: RegionParts = { province: '', city: '', district: '', detail: String(address || '').trim() }
  if (!regions.length || !parts.detail) return parts
  let rest = parts.detail

  let province: Region | null = null
  let city: Region | null = null

  const provinceToken = takeToken(rest, PROVINCE_TOKEN)
  if (provinceToken) {
    province = regions.find((item) => item.name === provinceToken.token) || null
    if (province) rest = provinceToken.rest
  }

  if (!province) {
    const cityToken = takeToken(rest, CITY_TOKEN)
    const hit = cityToken ? findCity(regions, cityToken.token) : null
    if (!hit) return parts
    province = hit.province
    city = hit.city
    rest = cityToken!.rest
  } else {
    const cityToken = takeToken(rest, CITY_TOKEN)
    const matched = cityToken
      ? (province.children || []).find((item) => item.name === cityToken.token) || null
      : null
    if (matched) {
      city = matched
      rest = cityToken!.rest
    }
  }

  parts.province = province.name
  parts.city = city ? city.name : ''

  const districtToken = takeToken(rest, DISTRICT_TOKEN)
  if (districtToken) {
    const pool = city
      ? city.children || []
      : (province.children || []).flatMap((item) => item.children || [])
    const district = pool.find((item) => item.name === districtToken.token)
    if (district) {
      parts.district = district.name
      rest = districtToken.rest
      if (!city) {
        const owner = (province.children || []).find((item) =>
          (item.children || []).some((child) => child.name === district.name)
        )
        if (owner) parts.city = owner.name
      }
    }
  }

  parts.detail = rest
  return parts
}

/**
 * 拼接完整地址，并去掉重复的省市区。
 *
 * 详细地址里重复写一遍省市区是最常见的手误（复制粘贴、逆地理编码结果直接粘进去），
 * 所以这里不是简单相加：先把详细地址开头与已选行政区重复的部分逐段剥掉，再拼。
 * 只认「带后缀的全名」和「后接下一个已选行政区的简称」，避免把「海南路」错当成「海南省」。
 */
export function joinAddress(province: string, city: string, district: string, detail: string): string {
  const picked: string[] = []
  for (const raw of [province, city, district]) {
    const name = String(raw || '').trim()
    if (!name) continue
    // 直辖市会出现省市同名（北京市 / 北京市），只保留一个；
    // 吉林省与吉林市这类同名不同级不能合并，所以要求两边都以「市」结尾
    if (picked.some((item) => sameLevelName(item, name))) continue
    picked.push(name)
  }

  let rest = String(detail || '').trim()
  for (let guard = 0; guard <= picked.length && rest; guard++) {
    const next = stripPickedPrefix(rest, picked)
    if (next === null) break
    rest = next
  }

  const head = picked.join('')
  if (!head) return rest
  return rest ? `${head}${rest}` : head
}

function takeToken(text: string, pattern: RegExp): { token: string; rest: string } | null {
  const body = String(text || '').replace(/^[\s,，、]+/, '')
  const matched = body.match(pattern)
  if (!matched || !matched[1]) return null
  return {
    token: matched[1],
    rest: body.slice(matched[1].length).replace(/^[\s,，、;；-]+/, '')
  }
}

function findCity(regions: Region[], name: string): { province: Region; city: Region } | null {
  for (const province of regions) {
    const city = (province.children || []).find((item) => item.name === name)
    if (city) return { province, city }
  }
  return null
}

/** 完全同名，或都是「市」且去后缀后同名（直辖市省/市同名） */
function sameLevelName(a: string, b: string): boolean {
  if (a === b) return true
  return /市$/.test(a) && /市$/.test(b) && stripSuffix(a) === stripSuffix(b)
}

function stripPickedPrefix(text: string, picked: string[]): string | null {
  const body = text.replace(/^[\s,，、]+/, '')
  if (!body) return null
  const candidates: Array<{ form: string; exact: boolean }> = []
  for (const name of picked) {
    candidates.push({ form: name, exact: true })
    const short = stripSuffix(name)
    if (short && short !== name) candidates.push({ form: short, exact: false })
  }
  candidates.sort((a, b) => b.form.length - a.form.length)

  for (const candidate of candidates) {
    if (!body.startsWith(candidate.form)) continue
    const rest = body.slice(candidate.form.length).replace(/^[\s,，、;；-]+/, '')
    if (candidate.exact || !rest) return rest
    // 简称后面必须紧跟另一个已选行政区，才算重复
    const followedByPicked = picked.some(
      (name) => name !== candidate.form && nameForms(name).some((form) => rest.startsWith(form))
    )
    if (followedByPicked) return rest
  }
  return null
}

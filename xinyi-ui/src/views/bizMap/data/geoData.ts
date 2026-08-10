import chinaGeo from './geo/china.json'
import guangdongCitiesGeo from './geo/guangdong-cities.json'
import guangdongOutlineGeo from './geo/guangdong-outline.json'
import guangxiCitiesGeo from './geo/guangxi-cities.json'
import guangxiOutlineGeo from './geo/guangxi-outline.json'
import maomingDistrictsGeo from './geo/maoming-districts.json'
import maomingOutlineGeo from './geo/maoming-outline.json'

/** 经纬度坐标 [经度, 纬度]。 */
export type LngLat = [number, number]

interface GeoGeometry {
  type: 'Polygon' | 'MultiPolygon'
  coordinates: unknown
}

interface GeoFeature {
  properties: {
    adcode: number
    name: string
    center?: number[]
    centroid?: number[]
  }
  geometry: GeoGeometry
}

interface GeoCollection {
  features: GeoFeature[]
}

/** 一个行政区可能由大陆主体和多个岛屿组成，因此使用多个外环。 */
export interface RegionArea {
  code: string
  name: string
  center: LngLat
  rings: LngLat[][]
  group?: '广东' | '广西'
}

export interface RegionPoint {
  code: string
  name: string
  center: LngLat
  group?: '广东' | '广西'
}

export interface MapContext {
  label: string
  rings: LngLat[][]
  focusRings: LngLat[][]
}

export interface MapLevel {
  code: string
  key: 'province' | 'city'
  name: string
  fullName: string
  subtitle: string
  /** 当前主视野的外边界。 */
  outlineRings: LngLat[][]
  /** 当前主视野中可点击的行政区。 */
  areas: RegionArea[]
  /** 行政区名称和点击锚点。 */
  points: RegionPoint[]
  /** 省级页用于全国方位定位的小地图。 */
  context?: MapContext
}

function outerRings(geometry: GeoGeometry): LngLat[][] {
  if (geometry.type === 'Polygon') {
    const polygon = geometry.coordinates as number[][][]
    return polygon.length ? [polygon[0] as LngLat[]] : []
  }
  const polygons = geometry.coordinates as number[][][][]
  return polygons.map((polygon) => polygon[0] as LngLat[]).filter((ring) => ring?.length >= 3)
}

function featureCenter(feature: GeoFeature): LngLat {
  const value = feature.properties.centroid || feature.properties.center
  return [Number(value?.[0] || 0), Number(value?.[1] || 0)]
}

function cityName(name: string) {
  return name.replace(/市$/, '')
}

function toAreas(collection: GeoCollection, group?: '广东' | '广西', normalize = false): RegionArea[] {
  return collection.features.map((feature) => ({
    code: String(feature.properties.adcode),
    name: normalize ? cityName(feature.properties.name) : feature.properties.name,
    center: featureCenter(feature),
    rings: outerRings(feature.geometry),
    group
  }))
}

function toPoints(areas: RegionArea[]): RegionPoint[] {
  return areas.map(({ code, name, center, group }) => ({ code, name, center, group }))
}

function collectionRings(collection: GeoCollection): LngLat[][] {
  return collection.features.flatMap((feature) => outerRings(feature.geometry))
}

const china = chinaGeo as unknown as GeoCollection
const guangdongCities = guangdongCitiesGeo as unknown as GeoCollection
const guangdongOutline = guangdongOutlineGeo as unknown as GeoCollection
const guangxiCities = guangxiCitiesGeo as unknown as GeoCollection
const guangxiOutline = guangxiOutlineGeo as unknown as GeoCollection
const maomingDistricts = maomingDistrictsGeo as unknown as GeoCollection
const maomingOutline = maomingOutlineGeo as unknown as GeoCollection

const GUANGDONG_AREAS = toAreas(guangdongCities, '广东', true)
const GUANGXI_AREAS = toAreas(guangxiCities, '广西', true)
const MAOMING_AREAS = toAreas(maomingDistricts)

const GUANGDONG_OUTLINE = collectionRings(guangdongOutline)
const GUANGXI_OUTLINE = collectionRings(guangxiOutline)
const MAOMING_OUTLINE = collectionRings(maomingOutline)

/** 一级：完整全国轮廓定位，主视野展示广东、广西地级市。 */
export const provinceLevel: MapLevel = {
  code: 'CN-SOUTH',
  key: 'province',
  name: '粤桂总览',
  fullName: '全国轮廓 · 广东 / 广西',
  subtitle: '粤桂地级市业务协同布局',
  outlineRings: [...GUANGDONG_OUTLINE, ...GUANGXI_OUTLINE],
  areas: [...GUANGDONG_AREAS, ...GUANGXI_AREAS],
  points: toPoints([...GUANGDONG_AREAS, ...GUANGXI_AREAS]),
  context: {
    label: '全国业务坐标',
    rings: collectionRings(china),
    focusRings: [...GUANGDONG_OUTLINE, ...GUANGXI_OUTLINE]
  }
}

/** 二级：茂名市只绘制县级边界，业务点最小到乡镇。 */
export const cityLevel: MapLevel = {
  code: '440900',
  key: 'city',
  name: '茂名市',
  fullName: '广东省 · 茂名市',
  subtitle: '县级区域与乡镇业务点分布',
  outlineRings: MAOMING_OUTLINE,
  areas: MAOMING_AREAS,
  points: toPoints(MAOMING_AREAS)
}

export const mapLevels: MapLevel[] = [provinceLevel, cityLevel]

/** 所有业务弧线均从信宜市中心出发。 */
export const CENTER_POINT = {
  code: '440983',
  name: '信宜市',
  label: '信宜市 · 业务中心',
  center: [110.947, 22.355] as LngLat
}

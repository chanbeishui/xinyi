// 机动车检测预约 · 地址簿共享类型（A02）。地址为车主本人 MINI 域私有数据，
// 后台不直接读写车主地址明文（隐私边界）；此处仅提供权威 DTO 类型供跨端复用。
export interface GeoPoint {
  lng: number
  lat: number
  coordinateSystem: 'GCJ02'
}

export interface Address {
  addressId: string
  label: string | null
  address: string
  location: GeoPoint
  isDefault: boolean
  version: number
}

export interface AddressCreate {
  label?: string
  address: string
  location: GeoPoint
  isDefault?: boolean
}

export interface AddressUpdate extends AddressCreate {
  expectedVersion: number
}
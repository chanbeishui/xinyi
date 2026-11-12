import { currentSession } from '../../services/http';

export type ForegroundPoint = {
  lat: number;
  lng: number;
  coordinateSystem: 'GCJ02';
  capturedAt: string;
  accuracyMeter: number | null;
};
let sampleScope = '',
  lastSampleAt = 0;

export function locationPlatform() {
  let supported = false;
  // #ifdef MP-WEIXIN
  supported = true;
  // #endif
  return supported;
}

/** 仅微信真机采点；H5没有已配置的可信GCJ02转换服务，不能把浏览器WGS84误标为GCJ02。 */
export function collectForegroundPoint(isCurrent: () => boolean): Promise<ForegroundPoint> {
  if (!locationPlatform())
    return Promise.reject(new Error('前台定位更新需使用微信真机；网页端仅查看服务端状态。'));
  if (!isCurrent()) return Promise.reject(new Error('当前页面已离开，未采集或上报位置'));
  const session = currentSession();
  const nextScope = session
    ? [session.accountId, session.sessionId, session.contextVersion, session.activeRole].join(':')
    : '';
  if (!nextScope || session?.activeRole !== 'DRIVER')
    return Promise.reject(new Error('当前代驾身份未就绪，未采集位置'));
  if (sampleScope !== nextScope) {
    sampleScope = nextScope;
    lastSampleAt = 0;
  }
  if (Date.now() - lastSampleAt < 30000)
    return Promise.reject(new Error('距前次采点尚不足30秒，请稍后主动重试；没有生成新的位置。'));
  lastSampleAt = Date.now();
  // uni回调没有统一的传感器采样时间，保守使用采点请求开始时刻，避免等待授权后把旧点刷新成新点。
  const capturedAt = new Date().toISOString();
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: 'gcj02',
      isHighAccuracy: true,
      highAccuracyExpireTime: 10000,
      success: (result) => {
        if (!isCurrent()) {
          reject(new Error('页面或身份已变化，已丢弃本次位置'));
          return;
        }
        if (
          !Number.isFinite(result.latitude) ||
          Math.abs(result.latitude) > 90 ||
          !Number.isFinite(result.longitude) ||
          Math.abs(result.longitude) > 180 ||
          (result.accuracy !== undefined &&
            result.accuracy !== null &&
            (!Number.isFinite(result.accuracy) || result.accuracy < 0))
        ) {
          reject(new Error('设备未返回可用的坐标或精度，本次位置没有上报'));
          return;
        }
        resolve({
          lat: result.latitude,
          lng: result.longitude,
          coordinateSystem: 'GCJ02',
          capturedAt,
          accuracyMeter:
            result.accuracy === undefined || result.accuracy === null
              ? null
              : Math.ceil(result.accuracy)
        });
      },
      fail: () =>
        reject(new Error('前台定位未完成。请核对微信隐私授权、位置权限及设备定位服务后主动重试。'))
    });
  });
}

/** 只读确认已经授予的位置权限，回到前台不自动弹出新的授权窗口。 */
export function locationAlreadyAuthorized(): Promise<boolean> {
  if (!locationPlatform()) return Promise.resolve(false);
  return new Promise((resolve) => {
    uni.getSetting({
      success: (result) => resolve(result.authSetting['scope.userLocation'] === true),
      fail: () => resolve(false)
    });
  });
}

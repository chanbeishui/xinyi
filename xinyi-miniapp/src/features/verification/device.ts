export type DeviceAuthMode = 'fingerPrint' | 'facial';
export type DeviceAuthentication = { resultJSON: string; resultJSONSignature: string };

export function deviceAuthPlatform() {
  let supported = false;
  // #ifdef MP-WEIXIN
  supported = true;
  // #endif
  return supported;
}

/** 本机已录入的生物特征仅用于当前设备确认，不能推导证件实名或活体核验。 */
export async function enrolledDeviceModes(): Promise<DeviceAuthMode[]> {
  if (!deviceAuthPlatform())
    throw new Error('本机生物认证需使用微信真机，网页端只可查看服务端进度。');
  const supported = await new Promise<DeviceAuthMode[]>((resolve, reject) => {
    uni.checkIsSupportSoterAuthentication({
      success: (result) =>
        resolve(
          result.supportMode.filter(
            (mode): mode is DeviceAuthMode => mode === 'fingerPrint' || mode === 'facial'
          )
        ),
      fail: () => reject(new Error('当前设备无法查询生物认证能力，请核对微信版本和设备设置'))
    });
  });
  const enrolled: DeviceAuthMode[] = [];
  for (const mode of supported) {
    const ready = await new Promise<boolean>((resolve) => {
      uni.checkIsSoterEnrolledInDevice({
        checkAuthMode: mode,
        success: (result) => resolve(result.isEnrolled),
        fail: () => resolve(false)
      });
    });
    if (ready) enrolled.push(mode);
  }
  if (!enrolled.length)
    throw new Error('本机尚无受支持且已录入的指纹或人脸，请先在设备设置中录入或更换受支持设备');
  return enrolled;
}

/** challenge 必须来自本次服务端会话。只回传原始签名材料，回调成功不构成业务通过。 */
export async function authenticateDevice(
  challenge: string,
  modes: DeviceAuthMode[],
  authContent: string
): Promise<DeviceAuthentication> {
  if (!deviceAuthPlatform() || !challenge || !modes.length)
    throw new Error('本机认证上下文未就绪，请重新读取');
  return new Promise((resolve, reject) => {
    uni.startSoterAuthentication({
      requestAuthModes: modes,
      challenge,
      authContent,
      success: (result) => {
        if (!result.resultJSON || !result.resultJSONSignature) {
          reject(new Error('设备认证未返回完整签名，请重新核对进度'));
          return;
        }
        resolve({ resultJSON: result.resultJSON, resultJSONSignature: result.resultJSONSignature });
      },
      fail: () => reject(new Error('设备认证未完成或已取消，资格状态没有因此改变'))
    });
  });
}

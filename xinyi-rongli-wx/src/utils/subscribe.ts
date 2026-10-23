/**
 * 微信订阅消息授权（V6.4 / PRD 23.6）。
 *
 * 设计前提：客户端拿到的 accept 只是“用户这一刻点了允许”的报告，不等于服务端获得发送许可，
 * 也不代表模板剩余次数足够。因此这里只做三件事：
 *   1. 向服务端申请本次可请求的已发布模板（服务端决定用哪个模板、哪一版、哪个 AppID）；
 *   2. 用服务端下发的 vendorTemplateId 调起微信订阅面板；
 *   3. 把本次结果带上 nonce 回传一次。
 *
 * 任何一步失败都不抛出：订阅不是下单、支付、交接、检测结果的前置条件，业务必须继续。
 * 不允许在这里“模拟”一次成功上报，避免把未授权写成已授权。
 */

export interface SubscribeResult {
  /** skipped：没有可请求模板或环境不支持；reported：已回传；failed：request 调起失败 */
  outcome: 'skipped' | 'reported' | 'failed'
  acceptedTemplateIds?: string[]
  note?: string
}

export async function requestSubscribe(
  commonApi: any,
  params: {
    businessObjectType: 'ORDER' | 'ASSIGNMENT' | 'STATEMENT' | 'DRIVER_APPLICATION'
    businessObjectId: number | string
    scenes: string[]
  }
): Promise<SubscribeResult> {
  let prompt: any = null
  try {
    prompt = await commonApi.createSubscriptionPrompt(params)
  } catch (e: any) {
    // 模板未发布、限流、无小程序凭据等都在这里，属于“本轮不请求订阅”，不是业务失败
    return { outcome: 'skipped', note: (e && e.message) || '当前没有可用的订阅模板' }
  }
  const templates: any[] = (prompt && prompt.templates) || []
  const tmplIds = templates.map((t) => String(t.vendorTemplateId)).filter(Boolean)
  if (!tmplIds.length) {
    return { outcome: 'skipped', note: '当前没有可用的订阅模板' }
  }

  // #ifdef MP-WEIXIN
  let results: any[] = []
  try {
    const res: any = await new Promise((resolve, reject) => {
      uni.requestSubscribeMessage({ tmplIds, success: resolve, fail: reject })
    })
    results = tmplIds.map((id) => ({ vendorTemplateId: id, result: res && res[id] ? res[id] : 'reject' }))
  } catch (e) {
    // 用户关闭面板或基础库不支持：不上报，避免把 reject 当成真实结果写库
    return { outcome: 'failed', note: '未完成订阅授权，可稍后再试' }
  }
  try {
    const reported: any = await commonApi.reportSubscriptionPrompt(prompt.promptId, {
      nonce: prompt.nonce,
      results
    })
    return { outcome: 'reported', acceptedTemplateIds: reported && reported.acceptedTemplateIds }
  } catch (e: any) {
    return { outcome: 'failed', note: (e && e.message) || '订阅结果回传失败' }
  }
  // #endif

  // #ifndef MP-WEIXIN
  return { outcome: 'skipped', note: '当前环境不支持微信订阅消息' }
  // #endif
}

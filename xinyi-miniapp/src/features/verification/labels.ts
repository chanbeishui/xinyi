export function verificationType(value: string) {
  return (
    (
      {
        DEVICE_AUTH: '本机生物认证',
        BACKGROUND: '背景核验',
        LICENSE: '驾驶证核验',
        TRAINING: '培训与考试'
      } as Record<string, string>
    )[value] || value
  );
}

export function verificationStatus(value: string) {
  return (
    (
      {
        CREATED: '待核验',
        PROCESSING: '核验处理中',
        PASSED: '已通过',
        FAILED: '未通过',
        EXPIRED: '已失效',
        UNAVAILABLE: '暂无有效核验结果',
        UNKNOWN: '结果尚未确认',
        CLOSED: '已封存'
      } as Record<string, string>
    )[value] || value
  );
}

export function verificationTime(value: string | null | undefined) {
  if (!value) return '暂无';
  const time = new Date(value);
  return Number.isNaN(time.getTime())
    ? value
    : time.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false });
}

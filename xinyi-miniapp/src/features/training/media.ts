import { currentSession } from '../../services/http';

export type TrainingMediaTicket = {
  downloadUrl: string;
  requiredHeaders: Record<string, string>;
};

const base = String(import.meta.env.VITE_API_BASE || '').replace(/\/$/, '');
const MAX_SIZE = 100 * 1024 * 1024;

/** 临时视频仅交给本地播放器；短票和登录令牌不进入 video.src 或持久化草稿。 */
export function releaseTrainingMedia(path: string) {
  if (!path) return;
  // #ifdef MP-WEIXIN
  uni.getFileSystemManager().unlink({ filePath: path, fail: () => {} });
  // #endif
  // #ifndef MP-WEIXIN
  if (path.startsWith('blob:')) URL.revokeObjectURL(path);
  // #endif
}

export function downloadTrainingMedia(
  ticket: TrainingMediaTicket,
  progress: (value: number) => void
) {
  const previous = currentSession();
  if (!previous) throw new Error('请先登录');
  if (!/^\/vi-private-transfer\/files\/[1-9][0-9]{0,18}$/.test(ticket.downloadUrl))
    throw new Error('受保护教材地址无效');
  if (!ticket.requiredHeaders['X-File-Ticket']) throw new Error('教材授权不完整，请重新核对课程');
  const unchanged = () => {
    const current = currentSession();
    return (
      current?.accountId === previous.accountId &&
      current?.sessionId === previous.sessionId &&
      current?.contextVersion === previous.contextVersion &&
      current?.activeRole === previous.activeRole
    );
  };
  let task: UniApp.DownloadTask | null = null;
  let cancelled = false;
  const promise = new Promise<string>((resolve, reject) => {
    task = uni.downloadFile({
      url: base + ticket.downloadUrl,
      timeout: 300000,
      header: {
        Authorization: 'Bearer ' + previous.token,
        'X-Client-Type': previous.activeRole.toLowerCase(),
        'X-File-Ticket': ticket.requiredHeaders['X-File-Ticket']
      },
      success: (result) => {
        if (cancelled || !unchanged()) {
          releaseTrainingMedia(result.tempFilePath);
          reject(new Error('下载已取消或身份已切换，请重新进入课程'));
        } else if (result.statusCode !== 200 || !result.tempFilePath) {
          releaseTrainingMedia(result.tempFilePath);
          reject(new Error('教材授权已失效或读取未完成，请重读课程后再试'));
        } else {
          progress(100);
          resolve(result.tempFilePath);
        }
      },
      fail: () => reject(new Error(cancelled ? '已取消教材下载' : '教材下载未完成，请重试'))
    });
    task.onProgressUpdate((value) => {
      if (
        !unchanged() ||
        value.totalBytesWritten > MAX_SIZE ||
        value.totalBytesExpectedToWrite > MAX_SIZE
      ) {
        cancelled = true;
        task?.abort();
        return;
      }
      if (!cancelled) progress(Math.max(0, Math.min(99, Math.floor(value.progress))));
    });
  });
  return {
    promise,
    cancel: () => {
      cancelled = true;
      task?.abort();
    }
  };
}

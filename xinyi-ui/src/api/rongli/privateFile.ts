import { getToken } from '@/utils/auth';
import { viPost } from './request';
import { intentKey, validId } from './aftersale';

const base = String(import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '');
export interface Voucher {
  fileId: string;
  name: string;
}
interface Transfer {
  requiredHeaders: Record<string, string>;
}
interface UploadTicket extends Transfer {
  uploadId: string;
  fileId: string;
  uploadUrl: string;
}
interface FileReady {
  fileId: string;
  status: string;
}
interface Access extends Transfer {
  downloadUrl: string;
}
type UploadTarget =
  | { purpose: 'DISPUTE'; orderNo: string }
  | { purpose: 'TRAINING_MEDIA'; trainingId: string };

function privateUrl(path: string) {
  // 文件票据不是公开下载凭据；只允许固定的同源传输地址，避免将后台令牌发送到第三方。
  if (!/^\/vi-private-transfer\/(uploads|files)\/[1-9][0-9]{0,18}$/.test(path))
    throw new Error('私有文件传输地址无效');
  const url = new URL(base + path, location.origin);
  if (url.origin !== location.origin) throw new Error('私有文件必须经同源代理传输');
  return url.href;
}
function transferHeaders(transfer: Transfer, token: string) {
  if (!transfer.requiredHeaders['X-File-Ticket']) throw new Error('文件授权不完整');
  return {
    Authorization: 'Bearer ' + token,
    'X-Client-Type': 'admin',
    'X-File-Ticket': transfer.requiredHeaders['X-File-Ticket'],
    ...(transfer.requiredHeaders['Content-Type']
      ? { 'Content-Type': transfer.requiredHeaders['Content-Type'] }
      : {})
  };
}
function current(token: string) {
  if (!token || getToken() !== token) throw new Error('登录已变化，请重新操作文件');
}
async function transfer<T>(
  url: string,
  options: RequestInit,
  consume: (response: Response) => Promise<T>,
  timeout = 45000
): Promise<T> {
  const controller = new AbortController();
  const deadline = window.setTimeout(() => controller.abort(), timeout);
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    return await consume(response);
  } finally {
    window.clearTimeout(deadline);
  }
}
async function failed(response: Response) {
  let message = '文件传输失败，请重新核对状态';
  try {
    const body = await response.json();
    message = body.msg || body.message || message;
  } catch {
    /* 代理可能返回非JSON，不能把HTML或内部响应直接显示给用户。 */
  }
  throw new Error(message);
}
export async function uploadVoucher(orderNo: string, image: File): Promise<Voucher> {
  if (
    !['image/jpeg', 'image/png'].includes(image.type) ||
    image.size <= 0 ||
    image.size > 10 * 1024 * 1024
  )
    throw new Error('请选择不超过 10MB 的 JPEG 或 PNG 图片');
  return uploadPrivateFile({ purpose: 'DISPUTE', orderNo }, image);
}
export async function uploadTrainingMedia(trainingId: string, video: File): Promise<Voucher> {
  if (video.type !== 'video/mp4' || video.size <= 0 || video.size > 100 * 1024 * 1024)
    throw new Error('请选择不超过 100MiB 的 MP4 视频');
  return uploadPrivateFile({ purpose: 'TRAINING_MEDIA', trainingId: validId(trainingId) }, video);
}
async function uploadPrivateFile(target: UploadTarget, file: File): Promise<Voucher> {
  const token = getToken() || '';
  current(token);
  const ticket = (
    await viPost<{ data: UploadTicket }>(
      '/api/common/uploads',
      {
        ...target,
        fileName: file.name,
        mimeType: file.type,
        size: file.size
      },
      { idempotencyKey: intentKey() }
    )
  ).data;
  current(token);
  try {
    await transfer(
      privateUrl(ticket.uploadUrl),
      {
        method: 'PUT',
        body: file,
        headers: transferHeaders(ticket, token),
        credentials: 'omit',
        cache: 'no-store',
        referrerPolicy: 'no-referrer',
        redirect: 'error'
      },
      async (response) => {
        if (!response.ok) await failed(response);
      },
      target.purpose === 'TRAINING_MEDIA' ? 300000 : 45000
    );
  } catch (error) {
    current(token);
    // 可能只丢失上传回执。先核对完成状态，只有 READY 文件可附加到业务记录。
    try {
      const result = (
        await viPost<{ data: FileReady }>(
          `/api/common/uploads/${validId(ticket.uploadId)}/complete`
        )
      ).data;
      current(token);
      if (result.status === 'READY') return { fileId: result.fileId, name: file.name };
    } catch {
      /* 原错误更能表达实际传输结果，未核验文件不会作为有效凭证返回。 */
    }
    throw error;
  }
  const result = (
    await viPost<{ data: FileReady }>(`/api/common/uploads/${validId(ticket.uploadId)}/complete`)
  ).data;
  current(token);
  if (result.status !== 'READY') throw new Error('文件尚未完成服务端核验');
  return { fileId: result.fileId, name: file.name };
}
export async function readVoucher(fileId: string, orderNo: string): Promise<string> {
  return readPrivateFile(fileId, { purpose: '售后经办人员核对业务凭证', orderNo }, false);
}
export async function readTrainingMedia(fileId: string): Promise<string> {
  return readPrivateFile(fileId, { purpose: '课程管理员核对私有培训教材' }, true);
}
async function readPrivateFile(
  fileId: string,
  input: { purpose: string; orderNo?: string },
  media: boolean
) {
  const token = getToken() || '';
  current(token);
  const access = (
    await viPost<{ data: Access }>(`/api/common/files/${validId(fileId)}/access`, input)
  ).data;
  current(token);
  const content = await transfer(
    privateUrl(access.downloadUrl),
    {
      headers: transferHeaders(access, token),
      credentials: 'omit',
      cache: 'no-store',
      referrerPolicy: 'no-referrer',
      redirect: 'error'
    },
    async (response) => {
      if (!response.ok) await failed(response);
      return response.blob();
    },
    media ? 300000 : 45000
  );
  current(token);
  if (
    !(media ? content.type === 'video/mp4' : ['image/jpeg', 'image/png'].includes(content.type)) ||
    content.size > (media ? 100 : 10) * 1024 * 1024
  )
    throw new Error('文件返回格式不正确');
  return URL.createObjectURL(content);
}

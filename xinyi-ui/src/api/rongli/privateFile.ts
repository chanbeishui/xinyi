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
  consume: (response: Response) => Promise<T>
): Promise<T> {
  const controller = new AbortController();
  const deadline = window.setTimeout(() => controller.abort(), 45000);
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
  const token = getToken() || '';
  current(token);
  if (
    !['image/jpeg', 'image/png'].includes(image.type) ||
    image.size <= 0 ||
    image.size > 10 * 1024 * 1024
  )
    throw new Error('请选择不超过 10MB 的 JPEG 或 PNG 图片');
  const ticket = (
    await viPost<{ data: UploadTicket }>(
      '/api/common/uploads',
      {
        purpose: 'DISPUTE',
        orderNo,
        fileName: image.name,
        mimeType: image.type,
        size: image.size
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
        body: image,
        headers: transferHeaders(ticket, token),
        credentials: 'omit',
        cache: 'no-store',
        referrerPolicy: 'no-referrer',
        redirect: 'error'
      },
      async (response) => {
        if (!response.ok) await failed(response);
      }
    );
  } catch (error) {
    current(token);
    // 可能只丢失上传回执。先核对完成状态，只有 READY 文件可附加到售后业务。
    try {
      const result = (
        await viPost<{ data: FileReady }>(
          `/api/common/uploads/${validId(ticket.uploadId)}/complete`
        )
      ).data;
      current(token);
      if (result.status === 'READY') return { fileId: result.fileId, name: image.name };
    } catch {
      /* 原错误更能表达实际传输结果，未核验文件不会作为有效凭证返回。 */
    }
    throw error;
  }
  const result = (
    await viPost<{ data: FileReady }>(`/api/common/uploads/${validId(ticket.uploadId)}/complete`)
  ).data;
  current(token);
  if (result.status !== 'READY') throw new Error('图片尚未完成服务端核验');
  return { fileId: result.fileId, name: image.name };
}
export async function readVoucher(fileId: string, orderNo: string): Promise<string> {
  const token = getToken() || '';
  current(token);
  const access = (
    await viPost<{ data: Access }>(`/api/common/files/${validId(fileId)}/access`, {
      purpose: '售后经办人员核对业务凭证',
      orderNo
    })
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
    }
  );
  current(token);
  if (!['image/jpeg', 'image/png'].includes(content.type) || content.size > 10 * 1024 * 1024)
    throw new Error('凭证返回格式不正确');
  return URL.createObjectURL(content);
}

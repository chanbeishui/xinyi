import { currentSession, request } from '../../services/http';

const base = String(import.meta.env.VITE_API_BASE || '').replace(/\/$/, '');
const MAX_SIZE = 10 * 1024 * 1024;
export type Voucher = { fileId: string; name: string };
type Transfer = { requiredHeaders: Record<string, string> };
type UploadTicket = Transfer & { uploadId: string; fileId: string; uploadUrl: string };
type Access = Transfer & { downloadUrl: string };
type FileReady = { fileId: string; status: 'UPLOADING' | 'VERIFYING' | 'READY' | 'REJECTED' };
type ChosenImage = { path: string; size: number; name: string; mime: string };

export function intentKey() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (character) => {
    const random = Math.floor(Math.random() * 16);
    return (character === 'x' ? random : (random & 3) | 8).toString(16);
  });
}

function context() {
  const session = currentSession();
  if (!session) throw new Error('请先登录');
  return session;
}
function unchanged(previous: ReturnType<typeof context>) {
  const session = context();
  if (
    previous.sessionId !== session.sessionId ||
    previous.contextVersion !== session.contextVersion
  )
    throw new Error('身份已切换，请重新操作文件');
  return session;
}
function transferUrl(path: string) {
  // 只接受本后端的固定传输路径，不把用户令牌交给外部地址或客户端提供的对象路径。
  if (!/^\/vi-private-transfer\/(uploads|files)\/[1-9][0-9]{0,18}$/.test(path))
    throw new Error('文件传输地址无效');
  return base + path;
}
function headers(required: Record<string, string>) {
  const session = context();
  if (!required['X-File-Ticket']) throw new Error('文件授权不完整');
  return {
    'X-File-Ticket': required['X-File-Ticket'],
    ...(required['Content-Type'] ? { 'Content-Type': required['Content-Type'] } : {}),
    'X-Client-Type': session.activeRole.toLowerCase(),
    Authorization: 'Bearer ' + session.token
  };
}

async function choose(): Promise<ChosenImage[]> {
  const result = await new Promise<UniApp.ChooseImageSuccessCallbackResult>((resolve, reject) =>
    uni.chooseImage({
      count: 1,
      sizeType: ['original'],
      sourceType: ['album', 'camera'],
      success: resolve,
      fail: () => reject(new Error('未选择图片'))
    })
  );
  return (
    result.tempFiles as UniApp.ChooseImageSuccessCallbackResult['tempFiles'] as Array<{
      path: string;
      size: number;
      name?: string;
      type?: string;
    }>
  ).map((file) => {
    const name = file.name || file.path.split('/').pop() || '凭证图片';
    const extension = name.toLowerCase().split('.').pop();
    const mime = file.type || (extension === 'png' ? 'image/png' : 'image/jpeg');
    if (!['image/jpeg', 'image/png'].includes(mime) || file.size <= 0 || file.size > MAX_SIZE)
      throw new Error('请选择不超过 10MB 的 JPEG 或 PNG 图片');
    return { path: file.path, size: file.size, name, mime };
  });
}
async function bytes(path: string): Promise<ArrayBuffer> {
  // #ifdef MP-WEIXIN
  return new Promise((resolve, reject) =>
    uni.getFileSystemManager().readFile({
      filePath: path,
      success: (result) => resolve(result.data as ArrayBuffer),
      fail: () => reject(new Error('无法读取所选图片，请重新选择'))
    })
  );
  // #endif
  // #ifndef MP-WEIXIN
  const response = await fetch(path);
  if (!response.ok) throw new Error('无法读取所选图片');
  return response.arrayBuffer();
  // #endif
}

/** 上传字节后再请求服务端核验；本地扩展名和客户端成功回调不能证明凭证已 READY。 */
export async function chooseAndUploadVoucher(orderNo: string): Promise<Voucher> {
  const previous = context();
  const [image] = await choose();
  unchanged(previous);
  const content = await bytes(image.path);
  unchanged(previous);
  const ticket = (
    await request<{ data: UploadTicket }>(
      'POST',
      '/api/common/uploads',
      {
        purpose: 'DISPUTE',
        orderNo,
        fileName: image.name,
        mimeType: image.mime,
        size: content.byteLength
      },
      intentKey()
    )
  ).data;
  unchanged(previous);
  try {
    await new Promise<void>((resolve, reject) =>
      uni.request({
        url: transferUrl(ticket.uploadUrl),
        method: 'PUT',
        data: content,
        header: headers(ticket.requiredHeaders),
        success: (response) => {
          if (response.statusCode >= 200 && response.statusCode < 300) resolve();
          else reject(new Error('文件传输未完成，请重新选择图片'));
        },
        fail: () => reject(new Error('上传结果未确认，正在核对文件状态'))
      })
    );
  } catch (error) {
    unchanged(previous);
    // 传输回执丢失时先尝试完成核验，避免把已收到的文件误报为未上传。
    try {
      const ready = (
        await request<{ data: FileReady }>(
          'POST',
          `/api/common/uploads/${ticket.uploadId}/complete`
        )
      ).data;
      unchanged(previous);
      if (ready.status === 'READY') return { fileId: ready.fileId, name: image.name };
    } catch {
      /* 保留原上传失败信息，服务端未核验的文件不能用于申请。 */
    }
    throw error;
  }
  unchanged(previous);
  const ready = (
    await request<{ data: FileReady }>('POST', `/api/common/uploads/${ticket.uploadId}/complete`)
  ).data;
  unchanged(previous);
  if (ready.status !== 'READY') throw new Error('文件尚未通过核验，请重新上传');
  return { fileId: ready.fileId, name: image.name };
}

export async function previewVoucher(fileId: string, orderNo: string) {
  if (!/^[1-9][0-9]{0,18}$/.test(fileId)) throw new Error('凭证编号无效');
  const previous = context();
  const access = (
    await request<{ data: Access }>('POST', `/api/common/files/${fileId}/access`, {
      purpose: '本人查看售后凭证',
      orderNo
    })
  ).data;
  unchanged(previous);
  const temp = await new Promise<string>((resolve, reject) =>
    uni.downloadFile({
      url: transferUrl(access.downloadUrl),
      header: headers(access.requiredHeaders),
      success: (response) => {
        if (response.statusCode === 200) resolve(response.tempFilePath);
        else reject(new Error('凭证访问已失效，请刷新后重试'));
      },
      fail: () => reject(new Error('凭证下载失败，请重试'))
    })
  );
  unchanged(previous);
  // 只向系统预览传递下载后的临时文件，短期授权与登录令牌不进入 image src 或草稿。
  uni.previewImage({ urls: [temp], current: temp });
}

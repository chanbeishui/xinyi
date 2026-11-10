import { getToken } from '@/utils/auth';
import { viGet, viPost } from './request';
import { validId } from './aftersale';

export type QualificationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'APPROVED'
  | 'REJECTED'
  | 'EXPIRED'
  | 'SUSPENDED';
export type AuditDecision = 'APPROVE' | 'REJECT' | 'REPAIR_REJECTION';
export const materialFields = [
  { field: 'realName', label: '姓名' },
  { field: 'idCardNo', label: '身份证号码' },
  { field: 'idCardFrontFileId', label: '身份证人像面' },
  { field: 'idCardBackFileId', label: '身份证国徽面' },
  { field: 'licenseMainFileId', label: '驾驶证主页' },
  { field: 'licenseSubFileId', label: '驾驶证副页' },
  { field: 'licenseClass', label: '准驾车型' },
  { field: 'firstIssueDate', label: '初领日期' },
  { field: 'validFrom', label: '驾驶证有效起始日' },
  { field: 'validTo', label: '驾驶证有效截止日' },
  { field: 'longTermValid', label: '驾驶证长期有效标记' }
] as const;
export type MaterialField = (typeof materialFields)[number]['field'];
export type EvidenceField =
  | 'idCardFrontFileId'
  | 'idCardBackFileId'
  | 'licenseMainFileId'
  | 'licenseSubFileId';
export interface SubmittedMaterials {
  realName: string | null;
  idCardNoMask: string | null;
  idCardNoProvided: boolean;
  idCardFrontFileId: string | null;
  idCardBackFileId: string | null;
  licenseMainFileId: string | null;
  licenseSubFileId: string | null;
  licenseClass: string | null;
  firstIssueDate: string | null;
  validFrom: string | null;
  validTo: string | null;
  longTermValid: boolean;
  consentVersion: string | null;
  consentTextHash: string | null;
  consentAcceptedAt: string | null;
}
export interface QualificationSummary {
  applicationId: string;
  driverId: string;
  applicantName: string | null;
  ownerDeptId: string | null;
  companyName: string | null;
  currentOwnerDeptId: string | null;
  currentCompanyName: string | null;
  companyRevision: number | null;
  status: QualificationStatus;
  effectiveQualificationStatus: QualificationStatus | null;
  profileVersion: number | null;
  version: number;
  submissionRevision: number;
  materialRevision: number;
  submittedAt: string | null;
  reviewDueAt: string | null;
  qualificationRevision: number | null;
  selfReviewDenied: boolean;
  hasTrustedSubmission: boolean;
  legacyRejectionRepairable: boolean;
  allowedActions: string[];
}
export interface EffectiveQualification {
  snapshotId: string;
  qualificationRevision: number;
  ownerDeptId: string;
  companyRevision: number;
  licenseClass: string;
  licenseValidTo: string | null;
  licenseLongTerm: boolean;
  reviewDueAt: string;
  expiresAt: string;
  approvedAt: string;
  status: QualificationStatus;
  effectiveStatus: QualificationStatus;
}
export interface QualificationReview {
  reviewId: string;
  decision: AuditDecision;
  submissionRevision: number;
  publicOpinion: string | null;
  rejectFields: MaterialField[];
  reviewedAt: string;
  reviewerUserId: string;
}
export interface QualificationDetail extends QualificationSummary {
  draft: SubmittedMaterials | null;
  items: string[];
  rejectFields: MaterialField[] | null;
  publicReviewOpinion: string | null;
  checks: Array<{ type: string; status: string }>;
  reviews: QualificationReview[];
  reviewBlockers: Array<{ field: string; reasonCode: string; message: string }>;
  submissionId: string | null;
  effectiveSnapshotId: string | null;
  effectiveQualification: EffectiveQualification | null;
}
export interface QualificationAudit {
  expectedVersion: number;
  expectedProfileVersion: number;
  expectedSubmissionRevision: number;
  expectedCompanyRevision: number;
  decision: AuditDecision;
  opinion: string;
  publicOpinion: string | null;
  rejectFields: MaterialField[];
  reviewDueAt: string | null;
}
const base = '/api/admin/driver-qualifications';
function detailResult(response: { code: number; data: QualificationDetail }, id: string) {
  const value = response?.data;
  if (
    response?.code !== 200 ||
    !value ||
    value.applicationId !== id ||
    !Array.isArray(value.allowedActions) ||
    !Number.isSafeInteger(value.version)
  )
    throw new Error('审核目标响应尚未确认，请重新读取');
  return value;
}
export async function listQualifications(params: object) {
  const response = await viGet<{ code: number; rows: QualificationSummary[]; total: number }>(
    base,
    params
  );
  if (
    response?.code !== 200 ||
    !Array.isArray(response.rows) ||
    !Number.isSafeInteger(response.total)
  )
    throw new Error('资格列表响应尚未确认');
  return response;
}
export async function getQualification(id: string) {
  return detailResult(
    await viGet<{ code: number; data: QualificationDetail }>(base + '/' + validId(id)),
    id
  );
}
export async function auditQualification(id: string, body: QualificationAudit, key: string) {
  return detailResult(
    await viPost<{ code: number; data: QualificationDetail }>(
      base + '/' + validId(id) + '/audit',
      body,
      { idempotencyKey: key }
    ),
    id
  );
}

export const qualificationLabel = (value: string | null | undefined) =>
  value
    ? (
        {
          DRAFT: '资料草稿',
          SUBMITTED: '待资格审核',
          APPROVED: '已批准',
          REJECTED: '已驳回',
          EXPIRED: '已到期',
          SUSPENDED: '已暂停',
          APPROVE: '批准资格',
          REJECT: '驳回并补正',
          REPAIR_REJECTION: '修复历史补正范围',
          DEVICE_AUTH: '本机生物认证',
          BACKGROUND: '背景人工核验',
          LICENSE: '驾驶证人工核验',
          TRAINING: '培训与考试',
          PASSED: '已通过',
          FAILED: '未通过',
          CREATED: '待核验',
          PROCESSING: '处理中',
          UNAVAILABLE: '无可信结果'
        } as Record<string, string>
      )[value] || value
    : '暂无';
export const fieldLabel = (value: string) =>
  materialFields.find((field) => field.field === value)?.label || value;
export const qualificationTime = (value: string | null | undefined) =>
  value
    ? new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false })
    : '暂无';

export function reviewDueInput(value: string | null) {
  if (!value) return '';
  const date = new Date(value);
  if (!Number.isFinite(date.getTime())) return '';
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23'
  }).formatToParts(date);
  const part = (key: string) => parts.find((p) => p.type === key)?.value;
  return `${part('year')}-${part('month')}-${part('day')} ${part('hour')}:${part('minute')}:${part('second')}`;
}

/** 日期输入明确为北京时间墙钟；不受审核员浏览器所在时区影响，接口始终提交UTC Z。 */
export function reviewDueInstant(value: string) {
  if (!/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(value))
    throw new Error('请选择北京时间的复审截止时刻');
  const date = new Date(value.replace(' ', 'T') + '+08:00');
  if (!Number.isFinite(date.getTime())) throw new Error('复审截止时刻无效');
  if (reviewDueInput(date.toISOString()) !== value)
    throw new Error('请选择有效的复审截止日期与时刻');
  if (date.getTime() <= Date.now())
    throw new Error('批准资格须选择未来的复审截止时刻，最终以服务端校验为准');
  return date.toISOString();
}

/** 证件仅从固定同源私有通道带票读取；目的由操作者填写，票据和图片均不进入持久缓存。 */
export async function readQualificationEvidence(fileId: string, purpose: string) {
  const token = getToken() || '';
  const current = () => {
    if (!token || getToken() !== token) throw new Error('登录已变化，请重新核对证件授权');
  };
  current();
  const response = await viPost<{
    code: number;
    data: { downloadUrl: string; requiredHeaders: Record<string, string> };
  }>('/api/common/files/' + validId(fileId) + '/access', { purpose });
  current();
  if (response.code !== 200 || !response.data || !response.data.requiredHeaders['X-File-Ticket'])
    throw new Error('证件访问授权尚未确认');
  const access = response.data;
  if (!/^\/vi-private-transfer\/files\/[1-9][0-9]{0,18}$/.test(access.downloadUrl))
    throw new Error('证件私有地址无效');
  const prefix = String(import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '');
  const url = new URL(prefix + access.downloadUrl, location.origin);
  if (url.origin !== location.origin) throw new Error('证件必须经同源私有代理读取');
  const controller = new AbortController(),
    deadline = window.setTimeout(() => controller.abort(), 45000);
  try {
    const result = await fetch(url.href, {
      headers: {
        Authorization: 'Bearer ' + token,
        'X-Client-Type': 'admin',
        'X-File-Ticket': access.requiredHeaders['X-File-Ticket']
      },
      credentials: 'omit',
      cache: 'no-store',
      referrerPolicy: 'no-referrer',
      redirect: 'error',
      signal: controller.signal
    });
    if (!result.ok)
      throw Object.assign(new Error('证件访问已失效或未获授权，请重新核对'), {
        httpStatus: result.status
      });
    const content = await result.blob();
    current();
    if (
      !['image/jpeg', 'image/png'].includes(content.type) ||
      content.size <= 0 ||
      content.size > 10 * 1024 * 1024
    )
      throw new Error('证件响应格式不正确');
    return URL.createObjectURL(content);
  } finally {
    window.clearTimeout(deadline);
  }
}

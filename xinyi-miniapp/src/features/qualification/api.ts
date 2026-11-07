import { currentSession, request } from '../../services/http';
import type { QualificationStatus } from '../../api/types';

export type EvidenceField =
  | 'idCardFrontFileId'
  | 'idCardBackFileId'
  | 'licenseMainFileId'
  | 'licenseSubFileId';

export type DraftValues = {
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
};

export type MyQualification = {
  applicationId: string;
  driverId: string;
  status: QualificationStatus;
  effectiveQualificationStatus: QualificationStatus | null;
  ownerDeptId: string | null;
  companyName: string | null;
  ownershipUnassigned: boolean;
  profileVersion: number;
  version: number;
  materialRevision: number;
  materialHash: string | null;
  submissionRevision: number;
  draft: DraftValues;
  editableFields: string[];
  rejectFields: string[] | null;
  publicReviewOpinion: string | null;
  submitBlockers: Array<{ field: string; reasonCode: string; message: string }>;
  checks: Array<{ type: string; status: string }>;
  allowedActions: string[];
  rejectReasons: string[];
  items: string[];
  reviewDueAt?: string | null;
  canAcceptOrder: false;
};

export type CreateQualification = {
  consentVersion: string;
  consentTextHash: string;
  acceptedConsent: true;
};
export type SaveQualification = {
  applicationId: string;
  expectedVersion: number;
  realName?: string | null;
  idCardNo?: string;
  idCardFrontFileId?: string | null;
  idCardBackFileId?: string | null;
  licenseMainFileId?: string | null;
  licenseSubFileId?: string | null;
  licenseClass?: string | null;
  firstIssueDate?: string | null;
  validFrom?: string | null;
  validTo?: string | null;
  longTermValid?: boolean;
  consentVersion?: string;
  consentTextHash?: string;
  acceptedConsent?: true;
};
export type SubmitQualification = {
  applicationId: string;
  expectedVersion: number;
  expectedProfileVersion: number;
  reason: string;
};

export type QualificationIntent =
  | { action: 'CREATE'; key: string; input: CreateQualification }
  | { action: 'SAVE'; key: string; input: SaveQualification }
  | { action: 'SUBMIT'; key: string; input: SubmitQualification };

const path = '/api/app/driver/qualification';
export async function getQualification(): Promise<MyQualification | null> {
  return (await request<{ data: MyQualification | null }>('GET', path)).data;
}

export async function runQualificationIntent(intent: QualificationIntent) {
  const response =
    intent.action === 'SAVE'
      ? await request<{ data: MyQualification }>('PUT', path, intent.input, intent.key)
      : await request<{ data: MyQualification }>(
          'POST',
          intent.action === 'SUBMIT' ? path + '/submit' : path,
          intent.input,
          intent.key
        );
  return response.data;
}

let pending: { scope: string; intent: QualificationIntent } | null = null;
function scope() {
  const session = currentSession();
  return session
    ? [session.accountId, session.sessionId, session.contextVersion, session.activeRole].join(':')
    : '';
}

/** 未知写入只在当前会话内保留原意图；身份证原文、登录凭据和文件票据都不落本地存储。 */
export function pendingQualificationIntent(intent?: QualificationIntent | null) {
  const current = scope();
  if (pending?.scope !== current) pending = null;
  if (intent !== undefined) pending = intent && current ? { scope: current, intent } : null;
  return pending?.intent || null;
}

export function qualificationStatus(value: string | null | undefined) {
  const labels: Record<string, string> = {
    DRAFT: '资料草稿',
    SUBMITTED: '审核中',
    APPROVED: '已审核通过',
    REJECTED: '审核未通过',
    EXPIRED: '资格已到期',
    SUSPENDED: '资格已暂停'
  };
  return value ? labels[value] || value : '尚未取得代驾资格';
}

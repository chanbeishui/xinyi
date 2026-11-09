import { viGet, viPost, viPut } from './request';
import { validId } from './aftersale';

export interface PolicyBlocker {
  field: string;
  reasonCode: string;
  message: string;
}
export interface PolicyRevision {
  revisionId: string;
  revisionNo: number;
  status: 'DRAFT' | 'PUBLISHED';
  requiredTrainingIds: string[];
  maxAttempts: number | null;
  retryIntervalSeconds: number | null;
  gradingMode: 'EQUAL_HALF_UP_EXACT';
  publishedAt: string | null;
  publishBlockers: PolicyBlocker[];
}
export interface TrainingPolicy {
  policyId: string | null;
  ownerDeptId: string;
  companyName: string;
  version: number;
  draftRevision: PolicyRevision | null;
  activeRevision: PolicyRevision | null;
  allowedActions: string[];
}
export interface SaveTrainingPolicy {
  expectedVersion: number;
  requiredTrainingIds: string[];
  maxAttempts: number | null;
  retryIntervalSeconds: number | null;
  reason: string;
}
export interface PublishTrainingPolicy {
  expectedVersion: number;
  draftRevisionId: string;
  reason: string;
}
const path = (company: string) => '/api/admin/training-policies/' + validId(company);
function result(response: { code: number; data: TrainingPolicy }, company: string) {
  const policy = response?.data;
  if (
    response?.code !== 200 ||
    !policy ||
    policy.ownerDeptId !== company ||
    !Number.isSafeInteger(policy.version) ||
    policy.version < 0 ||
    !Array.isArray(policy.allowedActions)
  )
    throw new Error('政策响应尚未确认，请重新核对当前公司');
  return policy;
}
export async function getTrainingPolicy(company: string) {
  return result(await viGet<{ code: number; data: TrainingPolicy }>(path(company)), company);
}
export async function saveTrainingPolicy(company: string, body: SaveTrainingPolicy, key: string) {
  return result(
    await viPut<{ code: number; data: TrainingPolicy }>(path(company), body, {
      idempotencyKey: key
    }),
    company
  );
}
export async function publishTrainingPolicy(
  company: string,
  body: PublishTrainingPolicy,
  key: string
) {
  return result(
    await viPost<{ code: number; data: TrainingPolicy }>(path(company) + '/publish', body, {
      idempotencyKey: key
    }),
    company
  );
}

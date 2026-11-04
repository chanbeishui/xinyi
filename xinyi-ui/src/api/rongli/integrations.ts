import { viGet, viPost, viPut } from './request'
export type Kind = 'PAYMENT_POLICY' | 'WECHAT_PAY' | 'WECHAT_APP'
export interface Issue { field: string; code: string; message: string; severity: string }
export interface Revision { revisionId: string; revision: number; contentHash: string; settings: { config: Record<string, any>; credentialStates: Record<string, { configured: boolean; lifecycle?: string }> } }
export interface Profile { profileId: string; name: string; kind: Kind; scope: { ownerDeptId: string; environment: 'TEST' | 'LIVE' }; version: number; activeRevisionId: string | null; draftRevisionId: string | null; active?: Revision; draft?: Revision; validationIssues: Issue[]; generatedPayNotifyUrl?: string | null; generatedRefundNotifyUrl?: string | null; historicalMaintenanceRequired?: boolean }
export interface Check { checkId: string; revisionId: string; status: 'PASS' | 'FAIL' | 'PARTIAL'; issues: Issue[]; expiresAt: string }
export const profiles = (params: object) => viGet<{ rows: Profile[]; total: number }>('/api/admin/integrations/profiles', params)
export const profile = (id: string) => viGet<{ data: Profile }>(`/api/admin/integrations/profiles/${id}`)
export const createProfile = (body: object, key: string) => viPost<{ data: Profile }>('/api/admin/integrations/profiles', body, { idempotencyKey: key })
export const saveDraft = (id: string, body: object, key: string) => viPut<{ data: Profile }>(`/api/admin/integrations/profiles/${id}/draft`, body, { idempotencyKey: key })
export const action = <T = any>(id: string, name: string, body: object, key: string) => viPost<{ data: T }>(`/api/admin/integrations/profiles/${id}/${name}`, body, { idempotencyKey: key })
export const publications = (id: string, pageNum: number) => viGet<{ rows: any[]; total: number }>(`/api/admin/integrations/profiles/${id}/publications`, { pageNum, pageSize: 10 })

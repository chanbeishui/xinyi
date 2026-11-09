import { currentSession, request } from '../../services/http';
import { intentKey } from '../file/api';

export type TrainingBlocker = { field: string; reasonCode: string; message: string };
export type TrainingUnit = {
  trainingId: string;
  courseKey: string;
  courseVersion: number;
  title: string;
  description: string | null;
  mediaFileId: string;
  mediaDurationMillis: number;
  requiredDurationSeconds: number;
  verifiedDurationSeconds: number;
  verifiedDurationMillis: number;
  status: string;
  latestAttemptId: string | null;
  version: number;
  initialized: boolean;
  attemptsUsed: number;
  maxAttempts: number;
  retryAvailableAt: string | null;
  allowedActions: string[];
};
export type Training = {
  applicationId: string;
  version: number;
  profileVersion: number;
  ownerDeptId: string | null;
  companyName: string | null;
  companyRevision: number;
  materialRevision: number;
  policyRevisionId: string | null;
  planId: string | null;
  runId: string | null;
  units: TrainingUnit[];
  complete: boolean;
  blockers: TrainingBlocker[];
  allowedActions: string[];
};
export type Playback = {
  playbackSessionId: string;
  applicationId: string;
  planId: string;
  runId: string;
  trainingId: string;
  courseVersion: number;
  serverStartedAt: string;
  expiresAt: string;
  version: number;
  progressVersion: number;
  heartbeatMaxGapSeconds: number;
};
export type PlaybackInput = {
  applicationId: string;
  expectedApplicationVersion: number;
  expectedCompanyRevision: number;
  expectedMaterialRevision: number;
  expectedPolicyRevisionId: string;
  planId: string | null;
  runId: string | null;
  courseVersion: number;
  expectedVersion: number;
};
export type ProgressInput = {
  applicationId: string;
  runId: string;
  courseVersion: number;
  expectedVersion: number;
  playbackSessionId: string;
  sequence: number;
  positionMillis: number;
  playing: boolean;
};
export type AttemptInput = {
  applicationId: string;
  runId: string;
  courseVersion: number;
  expectedVersion: number;
};
export type Question = {
  questionId: string;
  type: 'SINGLE' | 'MULTIPLE' | 'TRUE_FALSE';
  prompt: string;
  options: Array<{ code: string; text: string }>;
};
export type Attempt = {
  attemptId: string;
  applicationId: string;
  runId: string;
  trainingId: string;
  courseVersion: number;
  status: 'OPEN' | 'PASSED' | 'FAILED' | 'EXPIRED';
  questions: Question[];
  startedAt: string;
  expiresAt: string;
  submittedAt: string | null;
  score: number | null;
  passed: boolean | null;
  version: number;
  attemptNo: number;
  passScore: number;
  allowedActions: string[];
};
export type SubmitInput = {
  applicationId: string;
  runId: string;
  attemptId: string;
  expectedVersion: number;
  answers: Array<{ questionId: string; answerCodes: string[] }>;
};
const base = '/api/app/driver/qualification/training';
function id(value: string) {
  if (!/^[1-9][0-9]{0,18}$/.test(value)) throw new Error('培训记录编号无效');
  return value;
}
export const getTraining = async () => (await request<{ data: Training | null }>('GET', base)).data;
export const getUnit = async (value: string) =>
  (await request<{ data: TrainingUnit }>('GET', base + '/' + id(value))).data;
export const startPlayback = async (value: string, body: PlaybackInput, key: string) =>
  (await request<{ data: Playback }>('POST', base + '/' + id(value) + '/playback', body, key)).data;
export const reportProgress = async (value: string, body: ProgressInput, key: string) =>
  (await request<{ data: TrainingUnit }>('POST', base + '/' + id(value) + '/progress', body, key))
    .data;
export const createAttempt = async (value: string, body: AttemptInput, key: string) =>
  (await request<{ data: Attempt }>('POST', base + '/' + id(value) + '/attempts', body, key)).data;
export const getAttempt = async (value: string) =>
  (await request<{ data: Attempt }>('GET', base + '/attempts/' + id(value))).data;
export const submitAttempt = async (body: SubmitInput, key: string) =>
  (await request<{ data: Attempt }>('POST', base + '/submit', body, key)).data;
export const getMediaAccess = async (fileId: string) =>
  (
    await request<{ data: { downloadUrl: string; requiredHeaders: Record<string, string> } }>(
      'POST',
      '/api/common/files/' + id(fileId) + '/access',
      { purpose: '本人资格培训学习' }
    )
  ).data;

export type WriteIntent<T> = { body: T; key: string };
let owner = '';
const writes = new Map<string, WriteIntent<unknown>>();
export function trainingScope() {
  const session = currentSession();
  if (!session) throw new Error('请先登录');
  return [session.accountId, session.sessionId, session.contextVersion, session.activeRole].join(
    ':'
  );
}
function scoped() {
  const next = trainingScope();
  if (owner !== next) {
    writes.clear();
    owner = next;
  }
}
/** 只在当前会话内保存未知写入原体；同一业务意图不换键，切身份即清理，不存媒体票据或标准答案。 */
export function pendingWrite<T>(resource: string, value?: WriteIntent<T> | null) {
  scoped();
  if (value === null) writes.delete(resource);
  else if (value) writes.set(resource, value);
  return (writes.get(resource) as WriteIntent<T> | undefined) || null;
}
export const newWrite = <T>(body: T): WriteIntent<T> => ({ body, key: intentKey() });
export function unknownWrite(failure: unknown) {
  const status = (failure as { status?: number }).status;
  return status === undefined || status >= 500;
}
export function statusLabel(value: string) {
  return (
    (
      {
        NOT_STARTED: '未开始',
        IN_PROGRESS: '学习中',
        READY_FOR_EXAM: '有效学时已达标',
        OPEN: '考试进行中',
        PASSED: '已通过',
        FAILED: '未通过',
        EXPIRED: '已超时'
      } as Record<string, string>
    )[value] || value
  );
}
export function displayTime(value: string | null | undefined) {
  return value
    ? new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false })
    : '暂无';
}

/** 返回已有页面而非重复压栈，避免学习、考试和结果往返耗尽小程序页面栈。 */
export function returnToTraining(url: string) {
  const [path, query = ''] = url.split('?');
  const route = path.replace(/^\//, '');
  const expected = query
    ? Object.fromEntries(
        query.split('&').map((item) => {
          const [key, value = ''] = item.split('=');
          return [key, decodeURIComponent(value)];
        })
      )
    : {};
  const pages = getCurrentPages() as Array<{
    route?: string;
    options?: Record<string, string>;
    $page?: { options?: Record<string, string> };
  }>;
  for (let index = pages.length - 2; index >= 0; index--) {
    const page = pages[index],
      options = page.options || page.$page?.options || {};
    if (
      page.route === route &&
      Object.entries(expected).every(([key, value]) => options[key] === value)
    ) {
      uni.navigateBack({ delta: pages.length - 1 - index, fail: () => uni.redirectTo({ url }) });
      return;
    }
  }
  uni.redirectTo({ url });
}

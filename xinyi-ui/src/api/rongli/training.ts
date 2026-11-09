import { viGet, viPost, viPut } from './request';
import { validId } from './aftersale';

export type QuestionType = 'SINGLE' | 'MULTIPLE' | 'TRUE_FALSE';
export interface QuestionOption {
  code: string;
  text: string;
}
export interface TrainingQuestion {
  questionCode: string;
  type: QuestionType;
  prompt: string;
  options: QuestionOption[];
  correctOptionCodes: string[];
}
export interface PublishBlocker {
  field: string;
  reasonCode: string;
  message: string;
}
export interface TrainingSummary {
  trainingId: string;
  ownerDeptId: string | null;
  companyName: string | null;
  courseKey: string;
  courseVersion: number;
  contentHash: string | null;
  sourceTrainingId: string | null;
  title: string;
  description: string | null;
  status: 'DRAFT' | 'PUBLISHED' | 'DISABLED';
  version: number;
  allowedActions: string[];
  publishedAt: string | null;
  requiredDurationSeconds: number | null;
  passScore: number | null;
  attemptTimeLimitSeconds: number | null;
  mediaFileId: string | null;
  questionCount: number;
  publishBlockers: PublishBlocker[];
}
export interface TrainingDetail extends TrainingSummary {
  questions: TrainingQuestion[];
  media: {
    fileId: string;
    status: string;
    mimeType: string | null;
    size: number | null;
    durationMillis: number | null;
    width: number | null;
    height: number | null;
    videoCodec: string | null;
    audioCodec: string | null;
    inherited: boolean;
  } | null;
}
export type CreateTraining =
  | { ownerDeptId: string; title: string }
  | { sourceTrainingId: string; expectedSourceVersion: number };
export interface SaveTraining {
  expectedVersion: number;
  reason: string;
  title: string;
  description: string | null;
  mediaFileId: string | null;
  requiredDurationSeconds: number | null;
  passScore: number | null;
  attemptTimeLimitSeconds: number | null;
  questions: TrainingQuestion[];
}
export interface PublishTraining {
  expectedVersion: number;
  reason: string;
}
const base = '/api/admin/training-courses';
export const listTraining = (query: Record<string, string | number>) =>
  viGet<{ rows: TrainingSummary[]; total: number }>(base, query);
export const getTraining = (id: string) =>
  viGet<{ data: TrainingDetail }>(base + '/' + validId(id));
export const createTraining = (input: CreateTraining, key: string) =>
  viPost<{ data: TrainingDetail }>(base, input, { idempotencyKey: key });
export const saveTraining = (id: string, input: SaveTraining, key: string) =>
  viPut<{ data: TrainingDetail }>(base + '/' + validId(id), input, { idempotencyKey: key });
export const publishTraining = (id: string, input: PublishTraining, key: string) =>
  viPost<{ data: TrainingDetail }>(base + '/' + validId(id) + '/publish', input, {
    idempotencyKey: key
  });

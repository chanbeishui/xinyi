import { viGet, viPatch, viPost } from './request';
export interface DriverAvailability {
  listeningEnabled: boolean;
  newAssignmentEnabled: boolean;
  qualificationValid: boolean;
  occupationType: string;
  holderId: string | null;
  availableForNewOrder: boolean;
  reason: string;
  version: number;
}
export interface DriverRecord {
  driverId: string;
  accountId: string;
  name: string | null;
  phoneMask: string | null;
  ownerDeptId: string | null;
  companyName: string | null;
  ownershipUnassigned: boolean;
  canAssignCompany: boolean;
  qualificationStatus: string;
  qualificationRevision?: number | null;
  newAssignmentEnabled: boolean;
  listening: boolean;
  assignmentId: string | null;
  occupied: boolean;
  licenseClass: string | null;
  licenseValidTo: string | null;
  licenseLongTerm?: boolean;
  reviewDueAt: string | null;
  reviewDueAtInstant?: string | null;
  completedAssignmentCount?: number | null;
  score: string | null;
  internalRemark: string | null;
  availability: DriverAvailability;
  version: number;
  allowedActions: string[];
}
export interface AssignmentRecord {
  assignmentId: string;
  driverId: string;
  orderNo: string;
  acceptedAt: string;
  endedAt: string | null;
  endReason: string | null;
  custody: string;
  released: boolean;
  feeEntryId: string | null;
}
export interface DriverWrite {
  expectedVersion: number;
  reason: string;
  internalRemark?: string;
  ownerDeptId?: string;
}
export interface CompanyNode {
  id: string | number;
  label: string;
  disabled?: boolean;
  children?: CompanyNode[];
}
function path(id: string) {
  if (!/^[1-9][0-9]{0,18}$/.test(id)) throw new Error('代驾档案编号无效');
  return '/api/admin/drivers/' + id;
}
export const listDrivers = (params: Record<string, string | number>) =>
  viGet<{ rows: DriverRecord[]; total: number }>('/api/admin/drivers', params);
export const getDriver = (id: string) => viGet<{ data: DriverRecord }>(path(id));
export const maintainDriver = (id: string, body: DriverWrite, key: string) =>
  viPatch<{ data: DriverRecord }>(path(id), body, { idempotencyKey: key });
export const disableDriver = (id: string, body: DriverWrite, key: string) =>
  viPost<{ data: DriverAvailability }>(path(id) + '/disable-new-assignments', body, {
    idempotencyKey: key
  });
export const enableDriver = (id: string, body: DriverWrite, key: string) =>
  viPost<{ data: DriverAvailability }>(path(id) + '/enable-new-assignments', body, {
    idempotencyKey: key
  });
export const driverAssignments = (id: string, params: Record<string, string | number>) =>
  viGet<{ rows: AssignmentRecord[]; total: number }>(path(id) + '/assignments', params);
interface CompanyDepartment {
  deptId: string | number;
  parentId: string | number | null;
  deptName: string;
  status: string;
}
function departmentId(value: string | number | null) {
  if (value === null) return '0';
  if (typeof value === 'number' && !Number.isSafeInteger(value))
    throw new Error('部门编号超出客户端安全精度，请联系管理员核对目录接口');
  const id = String(value);
  if (!/^(0|[1-9][0-9]{0,18})$/.test(id)) throw new Error('公司部门目录编号无效');
  return id;
}
export async function companyTree() {
  // 复用框架实际存在的部门列表；legacy HTTP200中的业务拒绝也必须拒绝，不能当空目录成功。
  const result = await viGet<{ code: number; msg?: string; data: CompanyDepartment[] }>(
    '/system/dept/list'
  );
  if (result.code !== 200 || !Array.isArray(result.data))
    throw new Error(result.msg || '公司部门目录读取失败，请核对部门列表权限');
  const source = result.data.filter((department) => department.status === '0');
  const parents = new Map<string, string>();
  const nodes = new Map<string, CompanyNode>();
  source.forEach((department) => {
    const id = departmentId(department.deptId);
    if (id === '0') throw new Error('公司部门目录编号无效');
    parents.set(id, departmentId(department.parentId));
    nodes.set(id, { id, label: department.deptName, children: [] });
  });
  const roots: CompanyNode[] = [];
  for (const [id, node] of nodes) {
    const seen = new Set<string>([id]);
    let ancestor = parents.get(id);
    while (ancestor && nodes.has(ancestor)) {
      if (seen.has(ancestor)) throw new Error('公司部门目录存在循环，请联系管理员核对');
      seen.add(ancestor);
      ancestor = parents.get(ancestor);
    }
    const parent = nodes.get(parents.get(id) || '0');
    if (parent) parent.children!.push(node);
    else roots.push(node);
  }
  return { code: 200, data: roots };
}

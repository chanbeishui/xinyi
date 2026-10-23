import { ref, computed } from 'vue';
import { saveSession } from '../../services/http';
import { loginWechat } from './api';
import type { Session } from '../../api/types';

// O29 统一登录状态机：不弹注册角色选择，不接受客户端 role；新会话默认 OWNER，恢复真实资格。
export type LoginState = 'IDLE' | 'PROCESSING' | 'SUCCESS' | 'DENIED' | 'ERROR';

const LINK_HINTS: Record<string, string> = {
  NO_EMPLOYEE_MATCH: '',
  EMPLOYEE_LINKED: '已识别为员工，可进入检测工作。',
  WAIT_ADMIN_CONFIGURATION: '已识别员工，暂未配置检测工作权限，请联系管理员。',
  CONTACT_ADMIN: '员工关联已暂停，请联系管理员。',
  PHONE_REVERIFY_REQUIRED: '请重新完成微信手机号授权以恢复工作权限。',
  LINK_REVOKED: '员工关联已解除。'
};

function wxLoginCode(): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (r) => (r.code ? resolve(r.code) : reject(new Error('未获取到微信登录 code'))),
      fail: (e) => reject(new Error(e?.errMsg || '微信登录失败'))
    });
  });
}

export function useIdentityLogin() {
  const state = ref<LoginState>('IDLE');
  const message = ref('');
  const session = ref<Session | null>(null);

  const employeeHint = computed(() => {
    const key = session.value?.employeeLink?.messageKey;
    return key ? (LINK_HINTS[key] || '') : '';
  });

  async function login(): Promise<void> {
    state.value = 'PROCESSING';
    message.value = '';
    try {
      // wx.login 临时 code → 服务端换取可信 AppID/OpenID；绝不传 role/openid
      const code = await wxLoginCode();
      const s = await loginWechat(code);
      saveSession(s);
      session.value = s;
      state.value = 'SUCCESS';
    } catch (e) {
      const msg = e instanceof Error ? e.message : '登录失败，请重试';
      state.value = /deny|cancel|拒绝|auth deny/i.test(msg) ? 'DENIED' : 'ERROR';
      message.value = msg;
    }
  }

  // 新会话默认落车主页（activeRole=OWNER）；工作视角切换由 U01/切角色任务处理，不在登录时自动切。
  function enterApp(): void {
    uni.reLaunch({ url: '/pages/index/index' });
  }

  return { state, message, session, employeeHint, login, enterApp };
}

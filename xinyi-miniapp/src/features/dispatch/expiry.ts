/**
 * 服务端绝对截止只用于撤回页面的可操作投影。
 * 本地时间不能确认后端已过期、延长邀约或授予接受资格，最终写入仍由服务端重验。
 */
export function createExpiryGuard(revokeProjection: () => void) {
  let timer: ReturnType<typeof setTimeout> | null = null;
  let generation = 0;
  function clear() {
    generation++;
    if (timer !== null) clearTimeout(timer);
    timer = null;
  }
  return {
    clear,
    arm(deadlines: readonly string[]) {
      clear();
      if (!deadlines.length) return;
      const remaining = Math.min(...deadlines.map((value) => Date.parse(value))) - Date.now();
      const current = generation;
      const expire = () => {
        if (current !== generation) return;
        clear();
        revokeProjection();
      };
      if (!Number.isFinite(remaining) || remaining <= 0) expire();
      else timer = setTimeout(expire, Math.min(remaining, 2147483647));
    }
  };
}

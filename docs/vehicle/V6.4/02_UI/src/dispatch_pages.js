/* D01可点击规则演示。所有公司、版本、可达和位置均为明确夹具，不调用生产接口或微信定位。 */
(function (U) {
  'use strict';
  const { panel: p, field: f, notice: n, button: b, goBtn: g, kv, badge: tag } = U;
  const oldAction = U.action, oldSettings = U.desktopPages.B24;
  const actions = (...items) => '<div class="flex wrap">' + items.join('') + '</div>';
  const rules = '切后台保留听单意愿；手动关闭或公司每日下班关闭，次日须本人手动开启。';
  function model() {
    return U.store.dispatch01 || (U.store.dispatch01 = {
      companies: {
        'COMPANY-DEMO-A': { name: '荣利甲公司（演示）', version: 0, configured: false,
          closingTime: null, locationMaxAgeSeconds: null, reachabilityMaxAgeSeconds: null,
          maxAccuracyMeter: null, locationReportIntervalSeconds: null },
        'COMPANY-DEMO-B': { name: '荣利乙公司（演示）', version: 2, configured: true,
          closingTime: '22:30', locationMaxAgeSeconds: 120, reachabilityMaxAgeSeconds: 120,
          maxAccuracyMeter: 100, locationReportIntervalSeconds: 30 }
      },
      companyId: 'COMPANY-DEMO-B', businessDate: '2026-11-12', listening: false,
      listeningVersion: 0, occupied: false, clientState: 'FOREGROUND', locationFresh: false,
      accuracyValid: false, reachable: true, qualificationValid: true, newAssignmentEnabled: true,
      until: null, pendingCompanies: {}, pendingListening: null, journey: false, preview: ''
    });
  }
  function baseReady(s) {
    return s.listening && s.locationFresh && s.accuracyValid && s.reachable &&
      s.qualificationValid && s.newAssignmentEnabled && !s.occupied;
  }
  function companyPrefix(company) { return 'd01cfg.' + company + '.'; }
  function fieldValue(company, name) {
    return U.get(companyPrefix(company) + name, model().companies[company][name] ?? '');
  }
  U.desktopPages.B24 = function (view) {
    if (view.state !== 'listening-company') {
      const page = oldSettings(view);
      page.body = p('公司听单设置', n(rules) + g('按公司维护下班与位置参数', 'B24-listening-company')) + page.body;
      return page;
    }
    const s = model(), company = U.get('d01cfg.company', ''), current = s.companies[company];
    const permitted = U.get('d01cfg.permission', 'EDIT') === 'EDIT';
    const pending = s.pendingCompanies[company], prefix = companyPrefix(company);
    const form = current ? p(current.name,
      kv('公司编号', company) + kv('生效配置版本', current.version) + kv('配置状态', current.configured ? '已配置' : '尚未配置') +
      '<div class="form-grid">' +
      f('每日下班时间（北京时间）', prefix + 'closingTime', fieldValue(company, 'closingTime'), 'text', { required: true, placeholder: 'HH:mm', disabled: !permitted || !!pending }) +
      [['locationMaxAgeSeconds', '位置有效期（秒）', 31, 86400], ['reachabilityMaxAgeSeconds', '近期可达有效期（秒）', 31, 86400],
        ['maxAccuracyMeter', '最大位置误差（米）', 1, 100000], ['locationReportIntervalSeconds', '前台上报间隔（秒）', 30, 3600]]
        .map(([key, label, min, max]) => f(label, prefix + key, fieldValue(company, key), 'number',
          { required: true, min, max, disabled: !permitted || !!pending })).join('') + '</div>' +
      f('维护原因（只用于本次保存）', prefix + 'reason', '', 'textarea', { required: true, maxlength: 500, disabled: !permitted || !!pending }) +
      n('参数全部明确填写，上报间隔须小于两个有效期。保存立即生效；提前下班收紧旧窗口，延后不自动延长旧开启。没有上班时间或次日自动恢复。') +
      (pending ? n('原保存结果尚未确认，原公司、版本、七键正文及请求键保持不变。', 'warn') + b('按原意图恢复保存演示', 'd01-retry-company', '', { payload: company }) :
        permitted ? b('保存并立即生效（演示）', 'd01-save-company', '', { payload: company }) : n('当前演示授权只读；表单选择不会授予真实权限。', 'warn'))) :
      p('明确维护公司', n('未选择公司时不读取业务配置，不默认账号主部门。'));
    return { title: '公司听单设置', desc: 'B24专用子页归D01；其余C01参数不因此视为已实现。',
      body: n('本页仅本地交互演示，不保存真实公司配置。生产页面按后台授权及真实公司范围校验。', 'warn') +
        p('公司与授权', f('维护公司（明确选择）', 'd01cfg.company', company, 'select', { options: [['', '请选择'], ...Object.entries(s.companies).map(([id, value]) => [id, value.name])] }) +
          f('当前授权（演示场景）', 'd01cfg.permission', permitted ? 'EDIT' : 'QUERY', 'select', { options: [['EDIT', '有权公司可维护'], ['QUERY', '仅查询']] }) +
          f('下次保存回执（演示）', 'd01cfg.mode', 'KNOWN', 'select', { options: [['KNOWN', '明确成功'], ['UNKNOWN', '响应未知']] })) + form,
      actions: g('返回系统配置', 'B24', 'secondary') };
  };
  U.mobilePages.D06 = function (view) {
    const s = model();
    if (!s.journey && s.preview !== view.id) {
      s.preview = view.id;
      s.occupied = view.state === 'occupied';
      s.listening = view.state !== 'offline';
      s.locationFresh = view.state !== 'offline';
      s.accuracyValid = view.state !== 'offline';
      s.reachable = true;
      s.until = s.listening ? '当日 22:30（演示）' : null;
    }
    const config = s.companies[s.companyId], ready = baseReady(s);
    return { title: '听单工作台', tabs: 'driver', active: 0,
      body: n('本页为明确状态夹具，不进行真实听单、定位或派单。') +
        p(s.occupied ? '有在手任务或待响应邀约' : s.listening ? '已开启听单意愿' : '当前未开启听单',
          kv('所属公司', config.name) + kv('上海业务日', s.businessDate) +
          kv('听单意愿', s.listening ? '已开启' : '已关闭') +
          kv('公司每日下班', config.closingTime || '未配置') + kv('本次固定截止', s.until || '暂无') + n(rules) +
          b(s.listening ? '关闭未来听单' : '本人开启今日听单', 'd01-toggle', '', { payload: s.listening ? 'OFF' : 'ON' })) +
        p('独立条件（演示投影）', kv('公司新邀约许可', s.newAssignmentEnabled ? '允许' : '暂停') +
          kv('资格', s.qualificationValid ? '当前有效' : '当前不满足') + kv('占用', s.occupied ? 'ASG-DEMO-451 · 在手承接' : '无占用') +
          kv('位置仍在有效期', s.locationFresh ? '是' : '否') + kv('位置精度符合配置', s.accuracyValid ? '是' : '否') +
          kv('近期联系凭据', s.reachable ? '仍有效，非永久在线承诺' : '已过期') + kv('最近客户端报告', s.clientState === 'BACKGROUND' ? '后台' : '前台') +
          n(ready ? '基础候选条件满足；仍须后续具体订单匹配及接受校验。' : '基础候选条件未全部满足；不自动改写本人听单意愿。', ready ? 'success' : 'warn')) +
        p('本人服务', kv('已完成服务单量', '—（原型不查询真实归还计数）') +
          n('个人邀约详情、车型/半径和接受事务归D02；此处不虚构邀约或接受成功。') +
          (s.occupied ? g('查看在手任务设计', 'D09', 'secondary') : g('查看本人服务记录设计', 'D24', 'secondary')) +
          n('关闭、下班到期或暂停只影响新邀约；占用、承接及车辆保管归还责任保持。', 'warn')) +
        p('前后台与截止演示', actions(b('演示切到后台', 'd01-background', 'secondary'), b('演示位置与联系过期', 'd01-stale', 'secondary'),
          b('演示下班自动关闭', 'd01-close', 'secondary'), b('演示次日仍未开启', 'd01-next-day', 'secondary')) +
          n('生产小程序仅微信真机在本工作台前台、本人主动授权时采点，页面隐藏即停止。H5仅展示服务端状态，不假装后台持续定位。')),
      footer: g('切换已有身份', 'U01', 'secondary') };
  };
  function applyCompany(company, body) {
    const current = model().companies[company];
    if (body.expectedVersion !== current.version) { U.toast('原公司配置版本已变化，请核对后重新读取'); return false; }
    const { expectedVersion, reason, ...values } = body;
    Object.assign(current, values, { version: expectedVersion + 1, configured: true });
    U.set(companyPrefix(company) + 'reason', '');
    delete model().pendingCompanies[company];
    U.toast('本地演示配置已保存并立即生效；没有真实后台写入');
    return true;
  }
  U.action = async function (action, payload, element) {
    const s = model();
    if (action === 'd01-save-company' || action === 'd01-retry-company') {
      const company = payload;
      if (!s.companies[company] || U.get('d01cfg.permission', 'EDIT') !== 'EDIT') { U.toast('未明确选择有权公司，不能保存'); return; }
      if (action === 'd01-retry-company') {
        const intent = s.pendingCompanies[company];
        if (intent) applyCompany(company, intent.body);
      } else {
        if (s.pendingCompanies[company]) { U.toast('请先核对原保存结果，不新建写入'); return; }
        const body = { expectedVersion: s.companies[company].version, closingTime: String(fieldValue(company, 'closingTime')).trim(),
          locationMaxAgeSeconds: Number(fieldValue(company, 'locationMaxAgeSeconds')), reachabilityMaxAgeSeconds: Number(fieldValue(company, 'reachabilityMaxAgeSeconds')),
          maxAccuracyMeter: Number(fieldValue(company, 'maxAccuracyMeter')), locationReportIntervalSeconds: Number(fieldValue(company, 'locationReportIntervalSeconds')),
          reason: String(U.get(companyPrefix(company) + 'reason', '')).trim() };
        if (!/^([01][0-9]|2[0-3]):[0-5][0-9]$/.test(body.closingTime) || !body.reason || body.reason.length > 500) { U.toast('请明确填写HH:mm每日下班及500字内原因'); return; }
        for (const [key, min, max] of [['locationMaxAgeSeconds', 31, 86400], ['reachabilityMaxAgeSeconds', 31, 86400], ['maxAccuracyMeter', 1, 100000], ['locationReportIntervalSeconds', 30, 3600]]) {
          if (!Number.isInteger(body[key]) || body[key] < min || body[key] > max) { U.toast('参数未填完整或超出允许范围'); return; }
        }
        if (body.locationReportIntervalSeconds >= body.locationMaxAgeSeconds || body.locationReportIntervalSeconds >= body.reachabilityMaxAgeSeconds) { U.toast('上报间隔须小于两个有效期'); return; }
        if (U.get('d01cfg.mode', 'KNOWN') === 'UNKNOWN') {
          s.pendingCompanies[company] = { body, key: 'DEMO-ORIGINAL-INTENT-' + company + '-' + body.expectedVersion };
        } else applyCompany(company, body);
      }
      U.testing.render('B24-listening-company');
      return;
    }
    if (action && action.startsWith('d01-')) {
      s.journey = true;
      if (action === 'd01-toggle') {
        if (payload === 'ON' && !s.companies[s.companyId].configured) { U.toast('公司配置未就绪，不套用默认时间'); return; }
        s.listening = payload === 'ON';
        s.listeningVersion++;
        s.until = s.listening ? '当日 ' + s.companies[s.companyId].closingTime + '（演示）' : null;
      }
      if (action === 'd01-background') s.clientState = 'BACKGROUND';
      if (action === 'd01-stale') { s.locationFresh = false; s.reachable = false; }
      if (action === 'd01-close' || action === 'd01-next-day') { s.listening = false; s.until = '原固定截止已到（演示）'; }
      if (action === 'd01-next-day') s.businessDate = '2026-11-13';
      U.testing.render(U.currentId());
      return;
    }
    return oldAction(action, payload, element);
  };
  if (U.flows.configuration && !U.flows.configuration.ids.includes('B24-listening-company')) U.flows.configuration.ids.push('B24-listening-company');
  document.addEventListener('change', (event) => {
    const field = event.target.closest('[data-field]');
    if (field && ['d01cfg.company', 'd01cfg.permission'].includes(field.dataset.field)) {
      U.testing.render('B24-listening-company');
    }
  });
  if (/^(D06|B24)(-|$)/.test(U.currentId())) U.testing.render(U.currentId());
})(window.UI);

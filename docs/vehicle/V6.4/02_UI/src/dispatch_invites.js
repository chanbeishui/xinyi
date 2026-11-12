/* D02关联D03/D04的可点击资料演示；不调用生产接口、定位或通知。真实实现与测试证据另见implementation/D02。 */
(function (U) {
  'use strict';
  const { panel: p, field: f, notice: n, button: b, goBtn: g, kv } = U;
  const oldAction = U.action,
    oldWork = U.mobilePages.D06,
    oldSettings = U.desktopPages.B24;
  const actions = (...values) => '<div class="flex wrap">' + values.join('') + '</div>';
  function state() {
    return (
      U.store.dispatch02 ||
      (U.store.dispatch02 = {
        status: 'WAIT_RESPONSE',
        version: 2,
        orderVersion: 12,
        pending: null,
        assigned: false,
        policyVersion: { A: 0, B: 4 },
        pendingPolicy: {}
      })
    );
  }
  const labels = {
    WAIT_RESPONSE: '等待本人响应',
    ACCEPTED: '已接受',
    REJECTED: '已拒绝',
    EXPIRED: '已过期',
    REVOKED: '已撤回'
  };
  const hint = () =>
    n('明确演示夹具，不查询或更新真实订单、代驾、公司策略；不表示通知已送达。', 'warn');
  function inviteFields() {
    return (
      kv('原邀约 / 订单', '501 / VI-DEMO-20261112') +
      kv('服务公司 / 当前站点', '荣利甲公司（冻结） / 城南检测站') +
      kv('车型 / 接车区域', '小型汽车 / 上门接车') +
      kv('上门时段', '2026-11-12 14:00–15:00（演示）') +
      kv('接车点直线距离', '1.25公里（GCJ02直线，非导航路线）') +
      kv('响应绝对截止', '2026-11-12 12:01:00（演示）')
    );
  }
  U.mobilePages.D07 = function (view) {
    const s = state();
    if (view.state === 'list')
      return {
        title: '我的派单邀约',
        body:
          hint() +
          p('本人邀约', kv('状态', labels[s.status]) + inviteFields() + g('查看本次邀约', 'D07')) +
          (s.pending ? n('原响应未知；进入原邀约仅按原UUID与正文核对。', 'warn') : ''),
        footer: g('返回工作台', 'D06', 'secondary')
      };
    const waiting = s.status === 'WAIT_RESPONSE',
      unknown = !!s.pending;
    return {
      title: '本次派单邀约',
      body:
        hint() +
        p(
          labels[s.status],
          inviteFields() +
            n('邀请前不展示精确地址/坐标/联系方式；服务收费不是本单收入。') +
            (waiting && !unknown
              ? f('响应说明（可选，最多500字）', 'd02.reason', '', 'textarea', { maxlength: 500 }) +
                f('下次回执（演示）', 'd02.mode', 'KNOWN', 'select', {
                  options: [
                    ['KNOWN', '明确结果'],
                    ['UNKNOWN', '结果未知']
                  ]
                }) +
                actions(
                  b('接受本次邀约', 'd02-respond', '', { payload: 'ACCEPT' }),
                  b('拒绝本次邀约', 'd02-respond', 'secondary', { payload: 'REJECT' }),
                  b('演示原截止到达', 'd02-expire', 'secondary')
                )
              : '') +
            (unknown
              ? n(
                  '原响应结果未知。保留原目标、两个版本、会话观察值、六键正文及UUID；显示截止不擦除原意图。',
                  'warn'
                ) + b('核对并重试原响应（演示）', 'd02-retry')
              : s.status === 'ACCEPTED'
                ? n('原邀约永久映射承接801；该历史回执不能覆盖之后的新当前任务。', 'success') +
                  g('查看当前承接', 'D06-occupied')
                : !waiting
                  ? g('查看确定失效说明', 'D08')
                  : '') +
            n('最终资格、占用、订单和截止由服务端接受事务重新检查。页面隐藏不延长原邀约。')
        ),
      footer: g('返回本人邀约列表', 'D07-list', 'secondary')
    };
  };
  U.mobilePages.D08 = function () {
    const s = state();
    return {
      title: '原邀约状态',
      body:
        hint() +
        p(
          s.status === 'WAIT_RESPONSE' && !s.pending ? '已过期（确定失效演示）' : labels[s.status],
          inviteFields() +
            n(
              s.pending
                ? '未知不是确定失败；只核原回执，不创建新接受。'
                : '拒绝、过期或撤回都不能凭旧消息建立新承接。',
              'warn'
            )
        ),
      footer: g('返回本人邀约列表', 'D07-list')
    };
  };
  U.mobilePages.D06 = function (view) {
    const page = oldWork(view),
      s = state();
    page.body = page.body
      .replace(
        '基础候选条件满足；仍须后续具体订单匹配及接受校验。',
        '基础候选条件满足；具体订单仍须独立匹配与本人接受校验。'
      )
      .replaceAll('ASG-DEMO-451', '801')
      .replace(
        '个人邀约详情、车型/半径和接受事务归D02；此处不虚构邀约或接受成功。',
        '本人邀约与真实当前承接已接通；以下为明确本地演示。'
      )
      .replace(g('查看在手任务设计', 'D09', 'secondary'), '');
    page.body += p(
      '本人邀约与当前承接',
      g('查看本人派单邀约', 'D07-list') +
        (s.assigned || view.state === 'occupied'
          ? kv('上次查询的当前承接', '801 · 代驾已接单（演示）') +
            kv('原订单', 'VI-DEMO-20261112') +
            n(
              '退出、切身份、暂停听单不解除在手服务或车辆保管归还责任；本轮不提供D09出发/接车/交接按钮。',
              'warn'
            )
          : n('当前承接尚未查询；不以旧摘要推断已结束。'))
    );
    return page;
  };
  U.desktopPages.B05 = function (view) {
    const s = state();
    return {
      title: view.state === 'invite' ? '订单候选与人工邀约' : '派单队列',
      desc: '真实PC /dispatch/queue；计划104/105预览应用仍后续。',
      body:
        hint() +
        p(
          '冻结服务公司与订单',
          kv('订单', 'VI-DEMO-20261112 · 订单版本' + s.orderVersion) +
            kv('冻结公司 / 当前站点', '荣利甲公司 / 城南检测站') +
            kv('派单状态', '待人工派单 · 自动候选已轮完') +
            kv('原策略 / 批次截止', '策略5 / 2026-11-12 13:00（演示）') +
            n('旧订单始终使用建单冻结的服务公司与策略。站点迁移不漂移代驾池。')
        ) +
        (view.state === 'invite'
          ? p(
              '当前有效候选',
              kv('代驾昵称 / 掩码号码', '林师傅 / 139****0101') +
                kv('准驾类别 / 直线距离', 'C1 / 1.25公里') +
                kv('候选最早有效期', '2026-11-12 12:00:45（演示）') +
                f('人工邀约原因', 'd02.manualReason', '', 'textarea', {
                  required: true,
                  maxlength: 500
                }) +
                b('向所选代驾发出邀约（演示）', 'd02-manual') +
                n(
                  '原订单与代驾两个版本均重新读取；候选最早截止即撤旧可操作投影，人工也不绕资格/半径/占用或本人响应。'
                )
            )
          : g('候选与人工邀约', 'B05-invite')) +
        g('查看原订单邀约记录', 'B06'),
      actions: g('公司派单策略', 'B24-dispatch-company', 'secondary')
    };
  };
  U.desktopPages.B06 = function () {
    const s = state();
    return {
      title: '派单邀约记录',
      desc: '真实PC /dispatch/invites；不提供未实现导出。',
      body:
        hint() +
        p(
          '原邀约记录',
          kv('状态 / 来源', labels[s.status] + ' / 人工或自动') +
            inviteFields() +
            kv('邀约 / 订单版本', s.version + ' / ' + s.orderVersion) +
            (s.status === 'WAIT_RESPONSE'
              ? f('撤回原因', 'd02.revokeReason', '', 'textarea', {
                  required: true,
                  maxlength: 500
                }) + b('撤回本次等待邀约（演示）', 'd02-revoke', 'danger')
              : '') +
            n(
              '撤回仅释放原等待INVITE占用并转人工等待；已接受承接不能在此释放，不取消订单或处理退款。',
              'warn'
            )
        ),
      actions: g('返回调度队列', 'B05')
    };
  };
  U.desktopPages.B24 = function (view) {
    if (view.state !== 'dispatch-company') {
      const page = oldSettings(view);
      if (view.state === 'default')
        page.body =
          p('公司派单策略', g('维护公司派单参数与车型兼容', 'B24-dispatch-company')) + page.body;
      return page;
    }
    const s = state(),
      company = U.get('d02.company', ''),
      prefix = 'd02.policy.' + company + '.',
      pending = s.pendingPolicy[company],
      readOnly = U.get('d02.permission', 'EDIT') === 'QUERY';
    return {
      title: '公司派单策略',
      desc: '真实PC /dispatch/policy；B24其余C01能力不因此视为完成。',
      body:
        hint() +
        p(
          '明确维护公司',
          f('维护公司', 'd02.company', company, 'select', {
            options: [
              ['', '请选择有权公司'],
              ['A', '荣利甲公司（未配置演示）'],
              ['B', '荣利乙公司（已配置演示）']
            ]
          }) +
            f('授权场景', 'd02.permission', readOnly ? 'QUERY' : 'EDIT', 'select', {
              options: [
                ['EDIT', '有权编辑'],
                ['QUERY', '仅查询']
              ]
            }) +
            n('不默认账号主部门，缺配置不填写运营默认值。')
        ) +
        (company
          ? p(
              '公司策略版本' + s.policyVersion[company],
              '<div class="form-grid">' +
                [
                  ['inviteResponseSeconds', '邀约响应秒数'],
                  ['radiusMeter', '初始半径（米）'],
                  ['expandedRadiusMeter', '扩大后半径（米）'],
                  ['overallDeadlineSeconds', '批次总时限（秒）']
                ]
                  .map(([key, label]) =>
                    f(
                      label,
                      prefix + key,
                      U.get(
                        prefix + key,
                        company === 'B'
                          ? {
                              inviteResponseSeconds: 60,
                              radiusMeter: 3000,
                              expandedRadiusMeter: 6000,
                              overallDeadlineSeconds: 600
                            }[key]
                          : ''
                      ),
                      'number',
                      { required: true, disabled: readOnly || !!pending }
                    )
                  )
                  .join('') +
                '</div>' +
                kv('固定轮转 / 距离', '公司游标 COMPANY_CURSOR / GCJ02_GREAT_CIRCLE直线') +
                f('启用车型', 'd02.vehicle', 'SMALL', 'select', {
                  options: [['SMALL', '小型汽车（启用字典演示）']],
                  disabled: readOnly || !!pending
                }) +
                f(
                  '显式允许准驾类别（逗号分隔演示）',
                  prefix + 'licenseClasses',
                  U.get(prefix + 'licenseClasses', company === 'B' ? 'C1' : ''),
                  'text',
                  { required: true, disabled: readOnly || !!pending }
                ) +
                f('维护原因', prefix + 'reason', '', 'textarea', {
                  required: true,
                  maxlength: 500,
                  disabled: readOnly || !!pending
                }) +
                f('下次回执（演示）', 'd02.policyMode', 'KNOWN', 'select', {
                  options: [
                    ['KNOWN', '明确成功'],
                    ['UNKNOWN', '结果未知']
                  ]
                }) +
                (pending
                  ? n('原公司九键正文、版本与UUID已冻结，换公司不改变目标。', 'warn') +
                    b('重试原公司保存（演示）', 'd02-policy-retry', '', { payload: company })
                  : !readOnly
                    ? b('保存公司派单策略（演示）', 'd02-policy-save', '', { payload: company })
                    : '') +
                n(
                  '保存仅供新订单冻结；原订单/已启动批次不覆盖。初圈无候选只扩一圈，总截止优先单次响应。准驾业务集合由管理员明确维护，不推导法定准驾关系。'
                )
            )
          : ''),
      actions: g('返回系统配置', 'B24', 'secondary')
    };
  };
  U.action = async function (action, payload, element) {
    const s = state();
    if (action === 'd02-respond') {
      const body = {
        decision: payload,
        expectedVersion: s.version,
        expectedOrderVersion: s.orderVersion,
        expectedSessionId: 'DEMO-SESSION',
        expectedContextVersion: 1,
        reason: U.get('d02.reason', '') || null
      };
      if (s.status !== 'WAIT_RESPONSE' || s.pending) return;
      if (U.get('d02.mode', 'KNOWN') === 'UNKNOWN')
        s.pending = { body, key: 'DEMO-ORIGINAL-RESPONSE-501' };
      else {
        s.status = payload === 'ACCEPT' ? 'ACCEPTED' : 'REJECTED';
        s.assigned = payload === 'ACCEPT';
        s.version++;
      }
      U.testing.render('D07');
      return;
    }
    if (action === 'd02-retry') {
      if (s.pending) {
        s.status = s.pending.body.decision === 'ACCEPT' ? 'ACCEPTED' : 'REJECTED';
        s.assigned = s.pending.body.decision === 'ACCEPT';
        s.pending = null;
        s.version++;
      }
      U.testing.render('D07');
      return;
    }
    if (action === 'd02-expire') {
      s.status = 'EXPIRED';
      s.version++;
      U.testing.render('D08');
      return;
    }
    if (action === 'd02-manual') {
      if (!String(U.get('d02.manualReason', '')).trim()) {
        U.toast('请填写人工邀约原因');
        return;
      }
      s.status = 'WAIT_RESPONSE';
      s.assigned = false;
      s.version++;
      U.toast('仅本地演示发出，仍需本人接受');
      U.testing.render('B06');
      return;
    }
    if (action === 'd02-revoke') {
      if (s.status !== 'WAIT_RESPONSE' || !String(U.get('d02.revokeReason', '')).trim()) {
        U.toast('仅等待邀约且原因已填写时可撤回');
        return;
      }
      s.status = 'REVOKED';
      s.version++;
      U.testing.render('B06');
      return;
    }
    if (action === 'd02-policy-save' || action === 'd02-policy-retry') {
      const company = payload;
      if (!company || U.get('d02.permission', 'EDIT') !== 'EDIT') return;
      const prefix = 'd02.policy.' + company + '.';
      let body = s.pendingPolicy[company]?.body;
      if (!body) {
        body = {
          expectedVersion: s.policyVersion[company],
          inviteResponseSeconds: Number(U.get(prefix + 'inviteResponseSeconds', '')),
          radiusMeter: Number(U.get(prefix + 'radiusMeter', '')),
          expandedRadiusMeter: Number(U.get(prefix + 'expandedRadiusMeter', '')),
          overallDeadlineSeconds: Number(U.get(prefix + 'overallDeadlineSeconds', '')),
          roundPolicy: 'COMPANY_CURSOR',
          distanceMode: 'GCJ02_GREAT_CIRCLE',
          licenseCompatibility: [
            {
              vehicleTypeCode: 'SMALL',
              licenseClasses: String(U.get(prefix + 'licenseClasses', ''))
                .split(',')
                .map((x) => x.trim())
                .filter(Boolean)
            }
          ],
          reason: String(U.get(prefix + 'reason', '')).trim()
        };
        if (
          !Number.isInteger(body.inviteResponseSeconds) ||
          body.inviteResponseSeconds < 1 ||
          body.inviteResponseSeconds > 3600 ||
          !Number.isInteger(body.radiusMeter) ||
          body.radiusMeter < 1 ||
          body.radiusMeter > 1000000 ||
          !Number.isInteger(body.expandedRadiusMeter) ||
          body.expandedRadiusMeter < body.radiusMeter ||
          body.expandedRadiusMeter > 1000000 ||
          !Number.isInteger(body.overallDeadlineSeconds) ||
          body.overallDeadlineSeconds < 1 ||
          body.overallDeadlineSeconds > 86400 ||
          !body.licenseCompatibility[0].licenseClasses.length ||
          !body.reason
        ) {
          U.toast('请完整填写允许范围内参数、兼容关系和原因');
          return;
        }
      }
      if (action === 'd02-policy-save' && U.get('d02.policyMode', 'KNOWN') === 'UNKNOWN')
        s.pendingPolicy[company] = { body, key: 'DEMO-ORIGINAL-POLICY-' + company };
      else {
        s.policyVersion[company]++;
        delete s.pendingPolicy[company];
        U.set(prefix + 'reason', '');
        U.toast('本地演示已保存，新订单才冻结新策略');
      }
      U.testing.render('B24-dispatch-company');
      return;
    }
    return oldAction(action, payload, element);
  };
  document.addEventListener('change', (event) => {
    const field = event.target.closest('[data-field]');
    if (field && ['d02.company', 'd02.permission'].includes(field.dataset.field))
      U.testing.render('B24-dispatch-company');
  });
  if (/^(D06|D07|D08|B05|B06|B24)(-|$)/.test(U.currentId())) U.testing.render(U.currentId());
})(window.UI);

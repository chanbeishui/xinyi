package com.xinyi.rongli.order.service;

import com.xinyi.rongli.order.dto.*;
import com.xinyi.rongli.order.mapper.OrderReadMapper;
import com.xinyi.rongli.platform.actor.*;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.scope.service.EmployeeStationAccessService;
import com.xinyi.rongli.scope.mapper.EmployeeStationAccessMapper;
import com.xinyi.system.service.IDeptScopeService;
import com.xinyi.common.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class OrderReadService {
    private final BusinessActorResolver actors;
    private final OrderReadMapper mapper;
    private final OrderProjection projection;
    private final EmployeeStationAccessService station;
    private final EmployeeStationAccessMapper access;
    private final IDeptScopeService departments;
    private final AuditService audit;

    public OrderReadService(
            BusinessActorResolver actors,
            OrderReadMapper mapper,
            OrderProjection projection,
            EmployeeStationAccessService station,
            EmployeeStationAccessMapper access,
            IDeptScopeService departments,
            AuditService audit) {
        this.actors = actors;
        this.mapper = mapper;
        this.projection = projection;
        this.station = station;
        this.access = access;
        this.departments = departments;
        this.audit = audit;
    }

    public record Page(List<?> rows, long total) {}

    private boolean permitted(String p) {
        return SecurityUtils.hasPermi(p);
    }

    /** 无功能权限返回空范围；只有权限服务确认ALL时使用null，不能把空集当全量。 */
    private Set<Long> dept(String p) {
        if (!permitted(p)) return Set.of();
        var scope = departments.resolve(SecurityUtils.getLoginUser(), p);
        return scope.isAllData() ? null : new HashSet<>(scope.getDeptIds());
    }

    /** 按真实当前身份构建读取范围，财务/地址/详情权限独立计算，列表可见不等于详情可见。 */
    private OrderReadScope scope(String view, boolean detail) {
        String permission = detail ? "biz:order:query" : "biz:order:list";
        if ("ADMIN".equals(view)) {
            actors.requireAdmin();
            if (!permitted(permission)) throw new ViException(ViErrorCode.VI_FORBIDDEN);
            return new OrderReadScope(
                    view,
                    null,
                    null,
                    null,
                    dept(permission),
                    dept("biz:order:finance"),
                    dept("biz:order:address"),
                    permitted("biz:order:query"),
                    dept("biz:order:query"));
        }
        Actor actor = actors.requireMini();
        if (!view.equals(actor.actingRole())) throw new ViException(ViErrorCode.VI_FORBIDDEN);
        if ("STATION".equals(view)) {
            long id = station.require(permission);
            return new OrderReadScope(
                    view,
                    actor.accountId(),
                    null,
                    id,
                    access.orderDepartments(actor.accountId(), id, permission),
                    Set.of(),
                    Set.of(),
                    !access.orderDepartments(actor.accountId(), id, "biz:order:query").isEmpty(),
                    access.orderDepartments(actor.accountId(), id, "biz:order:query"));
        }
        Long driver = "DRIVER".equals(view) ? mapper.driverId(actor.accountId()) : null;
        if ("DRIVER".equals(view) && driver == null)
            throw new ViException(ViErrorCode.VI_FORBIDDEN);
        return new OrderReadScope(
                view, actor.accountId(), driver, null, null, Set.of(), Set.of(), true, null);
    }

    private <T> T guarded(String action, java.util.function.Supplier<T> work) {
        try {
            return work.get();
        } catch (RuntimeException e) {
            audit.recordFailure(
                    actors.currentActor(),
                    "VI_ORDER",
                    0,
                    action + "_REJECTED",
                    null,
                    Map.of(
                            "code",
                            e instanceof ViException v
                                    ? v.getErrorCode().name()
                                    : "VI_INTERNAL_ERROR"),
                    "订单访问被拒绝或失败");
            throw e;
        }
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Page list(String view, OrderQuery query) {
        return guarded(
                "ORDER_LIST",
                () -> {
                    var scope = scope(view, false);
                    return new Page(
                            mapper.list(scope, query).stream()
                                    .map(row -> projection.project(row, scope, false))
                                    .toList(),
                            mapper.count(scope, query));
                });
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    /** 先按权限范围加载原订单，再生成相应角色投影，避免通过更换订单号读取敏感信息。 */
    public Object detail(String view, String orderNo) {
        return guarded(
                "ORDER_DETAIL",
                () -> {
                    var scope = scope(view, true);
                    var row = mapper.detail(scope, orderNo);
                    if (row == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                    Object result = projection.project(row, scope, true);
                    audit.recordSuccess(
                            actors.currentActor(),
                            "VI_ORDER",
                            row.getId(),
                            "ORDER_DETAIL_READ",
                            null,
                            Map.of("view", view),
                            null);
                    return result;
                });
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Page timeline(String orderNo, OrderQuery query) {
        return guarded(
                "ORDER_TIMELINE",
                () -> {
                    var scope = scope("OWNER", true);
                    var row = mapper.detail(scope, orderNo);
                    if (row == null) throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                    // 当前仅登记已实现的公开事件；内部审计文本和未知事件不向车主透传。
                    var actions = Set.of("ORDER_CREATE");
                    var rows =
                            mapper
                                    .timeline(
                                            row.getId(), actions, query.offset(), query.pageSize())
                                    .stream()
                                    .map(
                                            m ->
                                                    new OrderViews.Timeline(
                                                            m.get("id").toString(),
                                                            orderNo,
                                                            m.get("action").toString(),
                                                            "预约已创建",
                                                            "车主",
                                                            m.get("at").toString()))
                                    .toList();
                    return new Page(rows, mapper.timelineCount(row.getId(), actions));
                });
    }
}

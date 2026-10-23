package com.xinyi.rongli.identity.auth;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色 → 能力(capabilities)/功能权限(permissions) 基线映射。字面量全部取自 V6.4 契约枚举
 * （04_接口/openapi_V6.4.yaml 的 CapabilityCode / PermissionCode 与权限与能力注册表）。
 *
 * 重要：这些只是 UI 提示（ADR-04）——服务端每个写操作仍按注册表与实时资格/站点/部门交集重新鉴权，
 * 前端不得据此放行。逐端点的精确能力门禁由各归属任务落地；本类只提供登录/档案返回的视角基线。
 */
public final class MiniCapabilityRegistry {

    private MiniCapabilityRegistry() {
    }

    /** 所有登录账号都具备的公共能力（公开浏览、本人档案、会话、手机号授权、员工关联读取）。 */
    private static final List<String> COMMON_CAPABILITIES = List.of(
            "COMMON_DICT_QUERY", "COMMON_FILES_ACCESS", "COMMON_FILES_QUERY",
            "COMMON_MESSAGES_LIST", "COMMON_MESSAGES_READ",
            "COMMON_ORDERS_CONFIRM", "COMMON_ORDERS_CONTACTACCESS", "COMMON_ORDERS_CREATE",
            "COMMON_ORDERS_LIST", "COMMON_ORDERS_QUERY",
            "COMMON_PHONE_CREATE", "COMMON_PHONE_SEND", "COMMON_PHONE_VERIFY",
            "COMMON_PRIVACYCASES_CREATE", "COMMON_PRIVACYCASES_LIST", "COMMON_STATIONS_LIST",
            "COMMON_SUBSCRIPTION_REPORT", "COMMON_SUBSCRIPTION_REQUEST",
            "COMMON_UPLOADS_COMPLETE", "COMMON_UPLOADS_CREATE",
            "PHONE_BIND", "PROFILE_READ", "SESSION_LOGOUT", "SESSION_READ",
            "SESSION_SWITCH_ROLE", "SESSION_SWITCH_STATION",
            "EMPLOYEE_LINK_READ", "EMPLOYEE_LINK_RECONCILE");

    private static final List<String> OWNER_CAPABILITIES = List.of(
            "OWNER_ADDRESSES_CREATE", "OWNER_ADDRESSES_DEFAULT", "OWNER_ADDRESSES_EDIT",
            "OWNER_ADDRESSES_LIST", "OWNER_ADDRESSES_REMOVE",
            "OWNER_AFTERSALE_CREATE", "OWNER_AFTERSALE_LIST", "OWNER_AFTERSALE_QUERY",
            "OWNER_OFFLINE_INTENT", "OWNER_ORDER_CLOSEUNPAID", "OWNER_ORDER_CREATE",
            "OWNER_ORDER_LIST", "OWNER_ORDER_QUERY", "OWNER_ORDER_SERVICECONFIRMATION",
            "OWNER_PAYMENT_OPTIONS", "OWNER_PROFILE_EDIT", "OWNER_SCHEDULE_LIST",
            "OWNER_VEHICLE_CREATE", "OWNER_VEHICLE_DEFAULT", "OWNER_VEHICLE_EDIT",
            "OWNER_VEHICLE_LIST", "OWNER_VEHICLE_REMOVE");

    private static final List<String> DRIVER_CAPABILITIES = List.of(
            "DRIVER_ASSIGNMENT_QUERY", "DRIVER_AVAILABILITY_EDIT", "DRIVER_AVAILABILITY_QUERY",
            "DRIVER_DRIVERPAYMENT_LIST", "DRIVER_DRIVERSTATEMENT_CONFIRM", "DRIVER_DRIVERSTATEMENT_CREATE",
            "DRIVER_DRIVERSTATEMENT_LIST", "DRIVER_DRIVERSTATEMENT_QUERY",
            "DRIVER_INVITE_LIST", "DRIVER_INVITE_QUERY", "DRIVER_INVITE_RESPONSE",
            "DRIVER_LOCATION_CREATE", "DRIVER_ORDER_ARRIVEPICKUP", "DRIVER_ORDER_ARRIVESTATION",
            "DRIVER_ORDER_ASSIGNMENTCANCELLATION", "DRIVER_ORDER_CREATE", "DRIVER_ORDER_DEPART",
            "DRIVER_ORDER_EDIT", "DRIVER_ORDER_LIST", "DRIVER_ORDER_QUERY", "DRIVER_ORDER_READY",
            "DRIVER_PENALTYAPPEAL_LIST", "DRIVER_PENALTYAPPEAL_QUERY", "DRIVER_PENALTY_CREATE",
            "DRIVER_PENALTY_LIST", "DRIVER_PENALTY_QUERY",
            "DRIVER_QUALIFICATION_ATTEMPTS", "DRIVER_QUALIFICATION_CREATE", "DRIVER_QUALIFICATION_EDIT",
            "DRIVER_QUALIFICATION_LIST", "DRIVER_QUALIFICATION_PLAYBACK", "DRIVER_QUALIFICATION_PROGRESS",
            "DRIVER_QUALIFICATION_QUERY", "DRIVER_QUALIFICATION_SUBMIT");

    private static final List<String> OWNER_PERMISSIONS = List.of(
            "biz:owner:edit", "biz:owner:list", "biz:owner:query",
            "biz:order:contact", "biz:order:create", "biz:order:list", "biz:order:query",
            "biz:vehicle:edit", "biz:vehicle:list", "biz:vehicle:query",
            "biz:aftersale:create", "biz:aftersale:list", "biz:aftersale:query",
            "biz:files:access", "biz:files:query", "biz:uploads:complete", "biz:uploads:create",
            "biz:dispute:list", "biz:dispute:query", "biz:refund:query",
            "biz:privacycase:list", "biz:privacycase:resolve");

    private static final List<String> DRIVER_PERMISSIONS = List.of(
            "biz:assignment:list",
            "biz:driverstatement:create", "biz:driverstatement:list", "biz:driverstatement:query",
            "biz:driverstatement:confirm",
            "biz:driverpayment:list", "biz:driverpayment:query",
            "biz:driver:list", "biz:driver:query");

    /** 按可用角色集合聚合能力（去重、保持稳定顺序）。 */
    public static List<String> capabilitiesFor(Set<String> roles) {
        Set<String> out = new LinkedHashSet<>(COMMON_CAPABILITIES);
        if (roles.contains("OWNER")) {
            out.addAll(OWNER_CAPABILITIES);
        }
        if (roles.contains("DRIVER")) {
            out.addAll(DRIVER_CAPABILITIES);
        }
        return new ArrayList<>(out);
    }

    /** 按可用角色集合聚合功能权限（去重、保持稳定顺序）。 */
    public static List<String> permissionsFor(Set<String> roles) {
        Set<String> out = new LinkedHashSet<>(OWNER_PERMISSIONS);
        if (roles.contains("DRIVER")) {
            out.addAll(DRIVER_PERMISSIONS);
        }
        return new ArrayList<>(out);
    }
}

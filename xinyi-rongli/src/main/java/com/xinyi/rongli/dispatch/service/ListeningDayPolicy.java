package com.xinyi.rongli.dispatch.service;

import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

/** 公司每日下班规则；每日开启是本人动作，后台切换和日期变化都不能自动开启。 */
public final class ListeningDayPolicy {
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private ListeningDayPolicy() {}

    /** 后台仅保存明确的24小时制HH:mm，不用服务器时区或示例时间补齐缺失配置。 */
    public static LocalTime closingTime(String value) {
        if (value == null || !value.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]")) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "请配置公司每日下班时间，格式为HH:mm");
        }
        return LocalTime.parse(value);
    }

    /** 开启时固定当日UTC截止。同日下班后拒绝开启，不能把截止悄悄滚到明天； 跨午夜后必须再次执行本人的开启动作，定时任务仅负责关闭。 */
    public static Instant deadlineForOpening(Instant now, LocalTime closingTime) {
        Objects.requireNonNull(now);
        Objects.requireNonNull(closingTime);
        Instant deadline =
                now.atZone(BUSINESS_ZONE)
                        .toLocalDate()
                        .atTime(closingTime)
                        .atZone(BUSINESS_ZONE)
                        .toInstant();
        if (!now.isBefore(deadline)) {
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "今天已到下班时间，请明天手动开启听单");
        }
        return deadline;
    }

    /**
     * 判定既有开启记录时绝不按今天重新计算截止。否则昨日未执行的关闭任务会在 次日误恢复听单。旧记录没有截止或公司epoch不符时也不能继续接收新邀约。
     * 前后台状态不参与本人听单意愿；位置与可达由独立规则判断。
     */
    public static boolean isListening(
            boolean manuallyEnabled,
            Instant openedUntil,
            Long openedCompanyRevision,
            Long currentCompanyRevision,
            Instant now) {
        Objects.requireNonNull(now);
        return manuallyEnabled
                && openedUntil != null
                && openedCompanyRevision != null
                && openedCompanyRevision.equals(currentCompanyRevision)
                && now.isBefore(openedUntil);
    }

    /** 延长公司下班时间不延长已经开启的当日记录；提前下班可收紧现有截止。 */
    public static Instant tightenedDeadline(Instant openedUntil, LocalTime currentClosingTime) {
        Objects.requireNonNull(openedUntil);
        Objects.requireNonNull(currentClosingTime);
        Instant current =
                openedUntil
                        .atZone(BUSINESS_ZONE)
                        .toLocalDate()
                        .atTime(currentClosingTime)
                        .atZone(BUSINESS_ZONE)
                        .toInstant();
        return current.isBefore(openedUntil) ? current : openedUntil;
    }
}

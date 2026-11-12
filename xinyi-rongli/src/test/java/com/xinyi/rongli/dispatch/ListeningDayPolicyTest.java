package com.xinyi.rongli.dispatch;

import static org.junit.jupiter.api.Assertions.*;

import com.xinyi.rongli.dispatch.service.ListeningDayPolicy;
import com.xinyi.rongli.platform.error.ViException;
import java.time.Instant;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ListeningDayPolicyTest {
    private static final Instant DEADLINE = Instant.parse("2026-11-12T10:00:00Z");

    @Test
    void explicitCompanyTimeRejectsMissingAndAmbiguousValues() {
        for (String value :
                new String[] {null, "", "6:00", "24:00", "18:60", "18:00:00", " 18:00"}) {
            assertThrows(ViException.class, () -> ListeningDayPolicy.closingTime(value));
        }
        assertEquals(LocalTime.of(18, 0), ListeningDayPolicy.closingTime("18:00"));
    }

    @Test
    void openingUsesShanghaiBusinessDateEvenWhenUtcIsPreviousDay() {
        assertEquals(
                DEADLINE,
                ListeningDayPolicy.deadlineForOpening(
                        Instant.parse("2026-11-11T17:00:00Z"), LocalTime.of(18, 0)));
    }

    @Test
    void exactClosingInstantAndLaterCannotRollOverToTomorrow() {
        assertThrows(
                ViException.class,
                () -> ListeningDayPolicy.deadlineForOpening(DEADLINE, LocalTime.of(18, 0)));
        assertThrows(
                ViException.class,
                () ->
                        ListeningDayPolicy.deadlineForOpening(
                                DEADLINE.plusSeconds(1), LocalTime.of(18, 0)));
        assertEquals(
                DEADLINE,
                ListeningDayPolicy.deadlineForOpening(
                        DEADLINE.minusMillis(1), LocalTime.of(18, 0)));
    }

    @Test
    void expirationIsEffectiveEvenWhenShutdownWorkerHasNotRun() {
        assertTrue(listening(true, DEADLINE.minusMillis(1)));
        assertFalse(listening(true, DEADLINE));
        assertFalse(listening(true, DEADLINE.plusSeconds(1)));
    }

    @Test
    void nextMorningNeverReactivatesYesterdayListening() {
        assertFalse(listening(true, Instant.parse("2026-11-13T01:00:00Z")));
        Instant tomorrow =
                ListeningDayPolicy.deadlineForOpening(
                        Instant.parse("2026-11-13T01:00:00Z"), LocalTime.of(18, 0));
        assertEquals(Instant.parse("2026-11-13T10:00:00Z"), tomorrow);
    }

    @Test
    void manualOffCannotBeOverriddenByUnexpiredWindow() {
        assertFalse(listening(false, DEADLINE.minusSeconds(3600)));
    }

    @Test
    void legacyOrTransferredCompanyCannotReuseAnOldOpening() {
        Instant now = DEADLINE.minusSeconds(1);
        assertFalse(ListeningDayPolicy.isListening(true, null, 1L, 1L, now));
        assertFalse(ListeningDayPolicy.isListening(true, DEADLINE, null, 1L, now));
        assertFalse(ListeningDayPolicy.isListening(true, DEADLINE, 1L, 2L, now));
        assertFalse(ListeningDayPolicy.isListening(true, DEADLINE, 1L, null, now));
    }

    @Test
    void companyChangeCanShortenButNeverExtendExistingDay() {
        assertEquals(
                DEADLINE.minusSeconds(3600),
                ListeningDayPolicy.tightenedDeadline(DEADLINE, LocalTime.of(17, 0)));
        assertEquals(DEADLINE, ListeningDayPolicy.tightenedDeadline(DEADLINE, LocalTime.of(19, 0)));
    }

    @Test
    void midnightClosingDoesNotMeanUnlimitedTwentyFourHours() {
        assertThrows(
                ViException.class,
                () ->
                        ListeningDayPolicy.deadlineForOpening(
                                Instant.parse("2026-11-12T16:00:00Z"), LocalTime.MIDNIGHT));
    }

    private boolean listening(boolean enabled, Instant now) {
        return ListeningDayPolicy.isListening(enabled, DEADLINE, 1L, 1L, now);
    }
}

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.LocalDateTime;

/** Pure Java reference boundary helpers; not a production Spring Service. */
public final class ContractScalars {
    private ContractScalars() {}
    public static long parseId(String text) {
        if (text == null || !text.matches("[1-9][0-9]{0,18}"))
            throw new IllegalArgumentException("ID must be a positive decimal string without leading zeros");
        try { return Long.parseLong(text); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("ID exceeds signed BIGINT", ex); }
    }
    /** Manual final price input has <= 2 decimals; do not silently round the user's text. */
    public static long yuanToFen(String text, boolean signed) {
        String pattern = signed ? "-?(0|[1-9][0-9]*)(\\.[0-9]{1,2})?" : "(0|[1-9][0-9]*)(\\.[0-9]{1,2})?";
        if (text == null || !text.matches(pattern)) throw new IllegalArgumentException("Invalid price format");
        return new BigDecimal(text).movePointRight(2).longValueExact();
    }
    /** Configured computations explicitly select rounding; not a global override of business rules. */
    public static long computedYuanToFen(BigDecimal yuan, RoundingMode roundingMode) {
        if (yuan == null || roundingMode == null) throw new IllegalArgumentException("Calculation arguments missing");
        return yuan.movePointRight(2).setScale(0, roundingMode).longValueExact();
    }
    public static Instant businessDayStart(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant();
    }
    public static LocalDateTime instantAsUtcColumns(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }
    public static Instant utcColumnsAsInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
    private static void rejects(Runnable fn) { try { fn.run(); } catch (IllegalArgumentException | ArithmeticException ex) { return; } throw new AssertionError("Invalid input accepted"); }
    public static void main(String[] args) {
        check(parseId("9223372036854775807") == Long.MAX_VALUE);
        rejects(() -> parseId("9223372036854775808")); rejects(() -> parseId("01")); rejects(() -> parseId("-1"));
        check(yuanToFen("0.01", false) == 1); check(yuanToFen("65.00", false) == 6500);
        check(yuanToFen("-5.00", true) == -500); rejects(() -> yuanToFen("0.001", false));
        rejects(() -> yuanToFen("-1.00", false)); rejects(() -> yuanToFen("1e2", false));
        check(businessDayStart("2026-10-20").equals(Instant.parse("2026-10-19T16:00:00Z")));
        Instant t=Instant.parse("2026-10-20T08:00:00.123Z"); check(utcColumnsAsInstant(instantAsUtcColumns(t)).equals(t));
        System.out.println("PASS: 12 pure scalar assertions. No MyBatis, database or HTTP integration executed.");
    }
}

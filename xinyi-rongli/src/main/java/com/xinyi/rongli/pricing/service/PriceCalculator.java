package com.xinyi.rongli.pricing.service;

import com.xinyi.rongli.pricing.dto.PriceContent;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import java.math.BigInteger;

/** 精确整数比例计算；HALF_UP仅发生一次，不使用浮点中间值。 */
public final class PriceCalculator {
    public static final long MAX_SAFE_FEN = 9_007_199_254_740_991L;
    private PriceCalculator() { }
    public record Amount(long distance, long excess, String units, long distanceFee, long total) { }

    public static Amount calculate(PriceContent c, long oneWayMeters) {
        if (oneWayMeters < 0 || oneWayMeters > Integer.MAX_VALUE || c.includedDistanceMeter() < 0
                || c.includedDistanceMeter() > Integer.MAX_VALUE || c.distanceUnitMeter() > Integer.MAX_VALUE
                || c.waitingFeeEnabled() || c.waitingFreeSeconds()!=0 || c.waitingUnitSeconds()!=0 || c.waitingUnitFee()!=0
                || c.distanceUnitFee() > MAX_SAFE_FEN || c.inspectionFee() > MAX_SAFE_FEN || c.driverBaseFee() > MAX_SAFE_FEN
                || c.distanceUnitMeter() < 1 || c.distanceUnitFee() < 0 || c.inspectionFee() < 0 || c.driverBaseFee() < 0) invalid();
        long distance;
        if ("ROUND_TRIP".equals(c.routeBasis())) distance = oneWayMeters * 2;
        else if ("ONE_WAY".equals(c.routeBasis())) distance = oneWayMeters;
        else { invalid(); return null; }
        long extra = Math.max(0, distance - c.includedDistanceMeter());
        BigInteger numerator = BigInteger.valueOf(extra).multiply(BigInteger.valueOf(c.distanceUnitFee()));
        BigInteger denominator = BigInteger.valueOf(c.distanceUnitMeter());
        BigInteger fee;
        String units;
        if ("CEIL_UNIT".equals(c.distanceRounding())) {
            long count = (extra + c.distanceUnitMeter() - 1) / c.distanceUnitMeter();
            fee = BigInteger.valueOf(count).multiply(BigInteger.valueOf(c.distanceUnitFee()));
            units = Long.toString(count);
        } else if ("EXACT_PRORATA".equals(c.distanceRounding())) {
            BigInteger[] division = numerator.divideAndRemainder(denominator);
            fee = division[0].add(division[1].shiftLeft(1).compareTo(denominator) >= 0 ? BigInteger.ONE : BigInteger.ZERO);
            units = extra + "/" + c.distanceUnitMeter();
        } else { invalid(); return null; }
        BigInteger total = fee.add(BigInteger.valueOf(c.inspectionFee())).add(BigInteger.valueOf(c.driverBaseFee()));
        if (total.compareTo(BigInteger.valueOf(MAX_SAFE_FEN)) > 0) invalid();
        return new Amount(distance, extra, units, fee.longValueExact(), total.longValueExact());
    }
    private static void invalid() { throw new ViException(ViErrorCode.VI_DATA_INVALID, "距离、计价参数无效或金额超出安全整数范围"); }
}

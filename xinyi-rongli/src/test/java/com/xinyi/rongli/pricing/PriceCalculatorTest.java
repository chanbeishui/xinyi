package com.xinyi.rongli.pricing;

import com.xinyi.rongli.pricing.dto.PriceContent;
import com.xinyi.rongli.pricing.service.PriceCalculator;
import com.xinyi.rongli.platform.error.ViException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {
    private PriceContent content(long included, long unit, long fee, String rounding, String route) {
        return new PriceContent("测试", "CAR", "ANNUAL", 20000, 3000, included, unit, fee, rounding, route,
                false, 0, 0, 0, null, null);
    }
    @Test void 包含里程边界不多收() {
        assertEquals(23000, PriceCalculator.calculate(content(1000,1000,100,"CEIL_UNIT","ONE_WAY"),1000).total());
        assertEquals(23100, PriceCalculator.calculate(content(1000,1000,100,"CEIL_UNIT","ONE_WAY"),1001).total());
    }
    @Test void 按比例半分四舍五入只计算一次() {
        assertEquals(1, PriceCalculator.calculate(content(0,1000,100,"EXACT_PRORATA","ONE_WAY"),5).distanceFee());
        assertEquals(0, PriceCalculator.calculate(content(0,1000,100,"EXACT_PRORATA","ONE_WAY"),4).distanceFee());
        assertEquals(2, PriceCalculator.calculate(content(0,1000,100,"EXACT_PRORATA","ONE_WAY"),15).distanceFee());
    }
    @Test void 往返先加倍距离再扣包含里程() {
        var result=PriceCalculator.calculate(content(1000,1000,100,"EXACT_PRORATA","ROUND_TRIP"),600);
        assertEquals(1200,result.distance());assertEquals(200,result.excess());assertEquals(20,result.distanceFee());
    }
    @Test void 大整数比例不因中间乘法溢出失真() {
        var result=PriceCalculator.calculate(content(0,Integer.MAX_VALUE,8_000_000_000_000_000L,"EXACT_PRORATA","ONE_WAY"),Integer.MAX_VALUE);
        assertEquals(8_000_000_000_023_000L,result.total());
    }
    @Test void 超安全金额拒绝而非返回负数或零() {
        assertThrows(ViException.class,()->PriceCalculator.calculate(content(0,1,PriceCalculator.MAX_SAFE_FEN,"CEIL_UNIT","ROUND_TRIP"),2));
        assertThrows(ViException.class,()->PriceCalculator.calculate(content(0,0,100,"CEIL_UNIT","ONE_WAY"),1));
        assertThrows(ViException.class,()->PriceCalculator.calculate(content(0,1000,100,"CEIL_UNIT","ONE_WAY"),-1));
    }
}

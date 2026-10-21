package com.xinyi.rongli.core;
import java.math.BigDecimal;
import java.math.RoundingMode;
/** 已确定金额到分；不从double创建，也不替业务确定费率。 */
public final class ViMoney {
 private ViMoney() {}
 public static long parseFen(String yuan) {
  if (yuan == null || !yuan.matches("(?:0|[1-9][0-9]*)(?:\\.[0-9]{1,2})?")) throw new IllegalArgumentException("INVALID_MONEY");
  return new BigDecimal(yuan).movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
 }
 public static long add(long a,long b){return Math.addExact(a,b);}
}

package com.xinyi.rongli.core;
public final class ViId {
 private ViId(){}
 public static long parse(String value){if(value==null || !value.matches("[1-9][0-9]{0,18}"))throw new IllegalArgumentException("INVALID_ID");return Long.parseLong(value);}
 public static String external(long id){if(id<=0)throw new IllegalArgumentException("INVALID_ID");return Long.toString(id);}
}

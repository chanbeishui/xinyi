package com.xinyi.rongli.core;
public class BoundarySelfTest {
 static void require(boolean x){if(!x)throw new AssertionError();}
 public static void main(String[] args){require(ViMoney.parseFen("0.01")==1);require(ViMoney.parseFen("65.00")==6500);require(ViId.external(ViId.parse("9007199254740993")).equals("9007199254740993"));
  try{ViMoney.parseFen("1.001");throw new AssertionError();}catch(IllegalArgumentException expected){}
  try{ViMoney.parseFen("-1");throw new AssertionError();}catch(IllegalArgumentException expected){}
  try{ViId.parse("9223372036854775808");throw new AssertionError();}catch(IllegalArgumentException expected){}
  System.out.println("6 boundary assertions PASS; not business integration");
 }
}

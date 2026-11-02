package com.xinyi.rongli.order.dto;
import java.time.Instant;
public record OrderQuery(String status,Instant startAt,Instant endAt,int pageNum,int pageSize){public int offset(){return Math.multiplyExact(pageNum-1,pageSize);}}

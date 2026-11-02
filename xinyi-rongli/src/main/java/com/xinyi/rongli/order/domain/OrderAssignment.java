package com.xinyi.rongli.order.domain;
import java.time.Instant;
public record OrderAssignment(Long id,Long orderId,Long driverId,Instant endedAt){}

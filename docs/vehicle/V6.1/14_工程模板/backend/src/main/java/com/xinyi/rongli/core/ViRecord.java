package com.xinyi.rongli.core;
import java.time.Instant;
/** 不是HTTP请求/响应DTO；由具体Mapper显式映射，禁止直接继承通用查询参数容器。 */
public abstract class ViRecord {
 private Long id; private Instant createdAt; private Instant updatedAt; private long version;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
 public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
 public long getVersion(){return version;} public void setVersion(long v){version=v;}
}

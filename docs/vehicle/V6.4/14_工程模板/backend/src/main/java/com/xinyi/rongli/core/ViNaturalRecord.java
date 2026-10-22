package com.xinyi.rongli.core;
import java.time.Instant;
/** 自然主键实体的时间与版本基类；driverId/scopeKey/guardKey由具体实体声明，不虚构id列。 */
public abstract class ViNaturalRecord {
 private Instant createdAt; private Instant updatedAt; private long version;
 public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
 public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
 public long getVersion(){return version;} public void setVersion(long v){version=v;}
}

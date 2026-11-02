package com.xinyi.rongli.integration.dto;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
public final class IntegrationRequests {
 private IntegrationRequests() {}
 public record Scope(String ownerDeptId,String environment) {}
 public record Create(String kind,String name,Scope scope) {}
 public record Draft(String kind,long expectedVersion,IntegrationSettings.Config config,Map<String,IntegrationSettings.SecretMutation> credentials,String reason,JsonNode publicConfig) {}
 public record Query(String kind,Long ownerDeptId,String environment,int pageNum,int pageSize) {public int offset(){return Math.multiplyExact(pageNum-1,pageSize);}}
}

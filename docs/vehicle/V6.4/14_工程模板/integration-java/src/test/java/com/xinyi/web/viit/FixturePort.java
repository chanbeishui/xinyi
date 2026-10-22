package com.xinyi.web.viit;
import java.util.Map;
/** E07必须实现此测试适配端口，调用真实框架/业务Service。不得提供返回固定成功的默认实现。 */
public interface FixturePort {
 Map<String,Object> create(String fixtureKind, String providerRunId);
 Map<String,Object> advanceClock(String runId,long advanceSeconds);
 Map<String,Object> runDue(String runId,String jobKind);
 Map<String,Object> applyProviderEvent(String runId,String rawBody,String signature);
}

package com.xinyi.web.viit;

import com.fasterxml.jackson.databind.JsonNode;
import com.xinyi.RuoYiApplication;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.error.ViException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes=RuoYiApplication.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
        "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
        "vi.environment=integration","vi.providers.mode=WECHAT","vi.outbox-relay-enabled=false","server.address=127.0.0.1"})
@ActiveProfiles("druid")
class C04PricingIT extends PricingHttpFixture {
    @Test void 草稿审核发布到同公司两个站点并产生真实审计() {
        String admin=rootToken(),reviewer=scoped(COMPANY_A,PERMS);long a=station(COMPANY_A),b=station(SITE_DEPT);
        JsonNode row=create(body(COMPANY_A,"TEST"),admin);
        assertEquals("DRAFT",row.get("status").asText());assertEquals(64,row.get("contentHash").asText().length());
        assertEquals(422,call("POST","/api/admin/prices/trial",match(a,1001),admin).getStatusCode().value());
        JsonNode trial=data(call("POST","/api/admin/prices/"+row.get("ruleId").asText()+"/trial",Map.of("distanceMeter",1001),admin));
        assertEquals(23100,trial.get("totalFen").asLong());assertFalse(trial.get("matchedPublished").asBoolean());
        row=submit(row,admin);assertEquals("IN_REVIEW",row.get("status").asText());
        row=approve(row,reviewer);assertEquals(row.get("contentHash"),row.get("approvedHash"));row=publish(row,admin);
        assertEquals("PUBLISHED",row.get("status").asText());
        JsonNode first=data(call("POST","/api/admin/prices/trial",match(a,1001),admin));
        JsonNode second=data(call("POST","/api/admin/prices/trial",match(b,1001),admin));
        assertEquals(first.get("ruleId"),second.get("ruleId"));assertEquals(23100,second.get("totalFen").asLong());
        assertTrue(second.get("matchedPublished").asBoolean());assertNotNull(pricing.quoteForStation(b,"C04_CAR","C04_ANNUAL",java.time.Instant.now(),1001));
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE aggregate_type='VI_PRICE_RULE' AND action!='PRICE_REJECTED'",Integer.class));
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_type='VI_PRICE_RULE'",Integer.class));
    }
    @Test void 提交人及编辑参与人不能自审且审核中不能改价() {
        String owner=scoped(COMPANY_A,PERMS),other=scoped(COMPANY_A,PERMS),reviewer=scoped(COMPANY_A,PERMS);
        Map<String,Object> input=body(COMPANY_A,"TEST");JsonNode draft=create(input,owner);String id=draft.get("ruleId").asText();
        input.put("expectedVersion",0);input.put("inspectionFee",20100);
        draft=data(call("PUT","/api/admin/prices/"+id,input,other));JsonNode submitted=submit(draft,owner);
        Map<String,Object> decision=Map.of("expectedVersion",submitted.get("version").asLong(),"decision","APPROVE","opinion","测试审核");
        for(String token:List.of(owner,other))assertEquals(403,call("POST","/api/admin/prices/"+id+"/audit",decision,token).getStatusCode().value());
        input.put("expectedVersion",submitted.get("version").asLong());
        assertEquals(409,call("PUT","/api/admin/prices/"+id,input,other).getStatusCode().value());
        JsonNode approved=approve(submitted,reviewer);assertEquals("APPROVED",approved.get("status").asText());
        input.put("expectedVersion",approved.get("version").asLong());assertEquals(409,call("PUT","/api/admin/prices/"+id,input,other).getStatusCode().value());
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='PRICE_REJECTED'",Integer.class));
    }
    @Test void 驳回保留草稿并且篡改内容不能沿用审核发布() {
        String admin=rootToken(),reviewer=scoped(COMPANY_A,PERMS);JsonNode row=submit(create(body(COMPANY_A,"TEST"),admin),admin);String id=row.get("ruleId").asText();
        row=data(call("POST","/api/admin/prices/"+id+"/audit",Map.of("expectedVersion",row.get("version").asLong(),"decision","REJECT","opinion","修改计费依据"),reviewer));
        assertEquals("DRAFT",row.get("status").asText());assertTrue(row.get("approvedHash").isNull());
        row=approve(submit(row,admin),reviewer);
        jdbc.update("UPDATE vi_price_rule SET config_json=JSON_SET(config_json,'$.inspectionFee',1) WHERE id=?",id);
        var result=call("POST","/api/admin/prices/"+id+"/publish",action(row),admin);
        assertEquals(409,result.getStatusCode().value());assertTrue(result.getBody().contains("VI_CONFIG_CHANGED"));
        assertEquals("APPROVED",jdbc.queryForObject("SELECT status FROM vi_price_rule WHERE id=?",String.class,id));
        JsonNode stopped=data(call("POST","/api/admin/prices/"+id+"/disable",action(row),admin));assertEquals("DISABLED",stopped.get("status").asText());assertTrue(stopped.get("config").isNull());
    }
    @Test void 越权列表详情写入及伪造字段拒绝() {
        String root=rootToken(),a=scoped(COMPANY_A,PERMS),b=scoped(COMPANY_B,PERMS);
        JsonNode row=create(body(COMPANY_A,"TEST"),root);String id=row.get("ruleId").asText();
        JsonNode listed=ViJson.tree(call("GET","/api/admin/prices",null,b).getBody());assertEquals(0,listed.get("total").asInt());
        assertEquals(403,call("POST","/api/admin/prices",body(COMPANY_A,"TEST"),b).getStatusCode().value());
        var update=body(COMPANY_A,"TEST");update.put("expectedVersion",0);
        assertEquals(404,call("PUT","/api/admin/prices/"+id,update,b).getStatusCode().value());
        update.put("ownerDeptId",Long.toString(COMPANY_B));assertEquals(403,call("PUT","/api/admin/prices/"+id,update,a).getStatusCode().value());
        update.put("approvedBy","1");assertEquals(400,call("PUT","/api/admin/prices/"+id,update,root).getStatusCode().value());
        assertEquals(400,call("GET","/api/admin/prices?params.dataScope=1",null,root).getStatusCode().value());
        assertEquals(404,call("POST","/api/admin/prices/"+id+"/trial",Map.of("distanceMeter",1000),b).getStatusCode().value());
        assertEquals(0,jdbc.queryForObject("SELECT version FROM vi_price_rule WHERE id=?",Integer.class,id));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox WHERE aggregate_type='VI_PRICE_RULE'",Integer.class));
    }
    @Test void 重复请求重放不同请求冲突及旧版本拒绝() {
        String root=rootToken();var body=body(COMPANY_A,"TEST");String key=UUID.randomUUID().toString();
        var first=callKey("POST","/api/admin/prices",body,root,key);JsonNode row=data(first);
        assertEquals(ViJson.tree(first.getBody()),ViJson.tree(callKey("POST","/api/admin/prices",body,root,key).getBody()));
        var changed=new LinkedHashMap<>(body);changed.put("inspectionFee",1);
        assertEquals(409,callKey("POST","/api/admin/prices",changed,root,key).getStatusCode().value());
        changed.put("expectedVersion",0);String path="/api/admin/prices/"+row.get("ruleId").asText();key=UUID.randomUUID().toString();
        var update=callKey("PUT",path,changed,root,key);assertEquals(200,update.getStatusCode().value(),update.getBody());
        assertEquals(ViJson.tree(update.getBody()),ViJson.tree(callKey("PUT",path,changed,root,key).getBody()));
        assertEquals(409,call("PUT",path,changed,root).getStatusCode().value());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_price_rule",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='PRICE_UPDATE'",Integer.class));
    }
    @Test void 无匹配或多匹配必须拒绝不猜最低价() {
        String root=rootToken(),reviewer=scoped(COMPANY_A,PERMS);long station=station(SITE_DEPT);
        assertEquals(422,call("POST","/api/admin/prices/trial",match(station,1000),root).getStatusCode().value());
        JsonNode original=publish(approve(submit(create(body(COMPANY_A,"TEST"),root),root),reviewer),root);
        JsonNode conflict=approve(submit(create(body(COMPANY_A,"TEST"),root),root),reviewer);
        assertEquals(409,call("POST","/api/admin/prices/"+conflict.get("ruleId").asText()+"/publish",action(conflict),root).getStatusCode().value());
        jdbc.update("UPDATE vi_price_rule SET status='PUBLISHED',published_at=UTC_TIMESTAMP(3) WHERE id=?",conflict.get("ruleId").asText());
        var ambiguous=call("POST","/api/admin/prices/trial",match(station,1000),root);assertEquals(409,ambiguous.getStatusCode().value());
        assertTrue(ambiguous.getBody().contains("VI_STATE_CONFLICT"));assertEquals("PUBLISHED",original.get("status").asText());
    }
    @Test void 新修订显式原子替换且不覆盖旧配置() {
        String root=rootToken(),reviewer=scoped(COMPANY_A,PERMS);long station=station(COMPANY_A);
        JsonNode old=publish(approve(submit(create(body(COMPANY_A,"TEST"),root),root),reviewer),root);String before=jdbc.queryForObject("SELECT config_json FROM vi_price_rule WHERE id=?",String.class,old.get("ruleId").asText());
        var next=body(COMPANY_A,"TEST");next.put("sourceRuleId",old.get("ruleId").asText());next.put("inspectionFee",21000);
        JsonNode row=approve(submit(create(next,root),root),reviewer);assertEquals(2,row.get("revision").asInt());
        Map<String,Object> publish=action(row);publish.put("replacesRuleId",old.get("ruleId").asText());publish.put("replacesExpectedVersion",old.get("version").asLong());
        row=data(call("POST","/api/admin/prices/"+row.get("ruleId").asText()+"/publish",publish,root));
        assertEquals("DISABLED",jdbc.queryForObject("SELECT status FROM vi_price_rule WHERE id=?",String.class,old.get("ruleId").asText()));
        assertEquals(before,jdbc.queryForObject("SELECT config_json FROM vi_price_rule WHERE id=?",String.class,old.get("ruleId").asText()));
        assertEquals(row.get("ruleId"),data(call("POST","/api/admin/prices/trial",match(station,0),root)).get("ruleId"));
        data(call("POST","/api/admin/prices/"+row.get("ruleId").asText()+"/disable",action(row),root));
        assertEquals(422,call("POST","/api/admin/prices/trial",match(station,0),root).getStatusCode().value());
    }
    @Test void 未来替换不能造成当前价格空窗且失败整体回滚() {
        String root=rootToken(),reviewer=scoped(COMPANY_A,PERMS);JsonNode old=publish(approve(submit(create(body(COMPANY_A,"TEST"),root),root),reviewer),root);
        var next=body(COMPANY_A,"TEST");next.put("sourceRuleId",old.get("ruleId").asText());next.put("effectiveFrom",java.time.Instant.now().plusSeconds(3600).truncatedTo(java.time.temporal.ChronoUnit.MILLIS).toString());
        JsonNode row=approve(submit(create(next,root),root),reviewer);var request=action(row);request.put("replacesRuleId",old.get("ruleId").asText());request.put("replacesExpectedVersion",old.get("version").asLong());
        assertEquals(409,call("POST","/api/admin/prices/"+row.get("ruleId").asText()+"/publish",request,root).getStatusCode().value());
        assertEquals("PUBLISHED",jdbc.queryForObject("SELECT status FROM vi_price_rule WHERE id=?",String.class,old.get("ruleId").asText()));
        assertEquals("APPROVED",jdbc.queryForObject("SELECT status FROM vi_price_rule WHERE id=?",String.class,row.get("ruleId").asText()));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='PRICE_PUBLISH'",Integer.class));
    }
    @Test void 按比例四舍五入往返单位边界和等待费拦截() {
        String root=rootToken();var body=body(COMPANY_A,"TEST");body.put("includedDistanceMeter",0);body.put("distanceRounding","EXACT_PRORATA");
        JsonNode row=create(body,root);String path="/api/admin/prices/"+row.get("ruleId").asText()+"/trial";
        assertEquals(23001,data(call("POST",path,Map.of("distanceMeter",5),root)).get("totalFen").asLong());
        assertEquals(23000,data(call("POST",path,Map.of("distanceMeter",4),root)).get("totalFen").asLong());
        body.put("routeBasis","ROUND_TRIP");body.put("expectedVersion",0);
        row=data(call("PUT","/api/admin/prices/"+row.get("ruleId").asText(),body,root));
        assertEquals(23001,data(call("POST",path,Map.of("distanceMeter",3),root)).get("totalFen").asLong());
        body.remove("expectedVersion");body.put("waitingFeeEnabled",true);
        assertEquals(422,call("POST","/api/admin/prices",body,root).getStatusCode().value());
        body.put("waitingFeeEnabled",false);body.put("inspectionFee",0.5);
        assertEquals(400,call("POST","/api/admin/prices",body,root).getStatusCode().value());
        assertEquals(400,call("POST",path,Map.of("distanceMeter",-1),root).getStatusCode().value());
        assertEquals(400,call("POST",path,Map.of("distanceMeter",1,"totalFen",0),root).getStatusCode().value());
    }
    @Test void 并发发布冲突只提交一个版本() throws Exception {
        String root=rootToken(),reviewer=scoped(COMPANY_A,PERMS);
        JsonNode a=approve(submit(create(body(COMPANY_A,"TEST"),root),root),reviewer);
        JsonNode b=approve(submit(create(body(COMPANY_A,"TEST"),root),root),reviewer);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);var gate=new java.util.concurrent.CountDownLatch(1);
        try {
            var x=pool.submit(()->{gate.await();return call("POST","/api/admin/prices/"+a.get("ruleId").asText()+"/publish",action(a),root).getStatusCode().value();});
            var y=pool.submit(()->{gate.await();return call("POST","/api/admin/prices/"+b.get("ruleId").asText()+"/publish",action(b),root).getStatusCode().value();});
            gate.countDown();var codes=List.of(x.get(10,java.util.concurrent.TimeUnit.SECONDS),y.get(10,java.util.concurrent.TimeUnit.SECONDS));
            assertTrue(codes.contains(200)&&codes.contains(409),codes.toString());
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_price_rule WHERE status='PUBLISHED'",Integer.class));
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox WHERE event_type='PRICE_PUBLISH'",Integer.class));
        }finally{pool.shutdownNow();}
    }
    @Test void 测试环境不能维护生产规则且未知归属旧记录不参与() {
        String root=rootToken();long station=station(COMPANY_A);
        assertEquals(422,call("POST","/api/admin/prices",body(COMPANY_A,"LIVE"),root).getStatusCode().value());
        jdbc.update("INSERT INTO vi_price_rule (rule_key,revision,vehicle_type_code,inspection_type_code,status,config_json,effective_from) VALUES ('C04_legacy',1,'C04_CAR','C04_ANNUAL','PUBLISHED','{}',UTC_TIMESTAMP(3))");
        assertEquals(0,ViJson.tree(call("GET","/api/admin/prices",null,root).getBody()).get("total").asInt());
        assertEquals(422,call("POST","/api/admin/prices/trial",match(station,0),root).getStatusCode().value());
    }
    @Test void 空部门范围关闭且权限撤销后的旧令牌不能写入() {
        String none=scopedWithScope(COMPANY_A,"5",PERMS);
        assertEquals(0,ViJson.tree(call("GET","/api/admin/prices",null,none).getBody()).get("total").asInt());
        assertEquals(403,call("POST","/api/admin/prices",body(COMPANY_A,"TEST"),none).getStatusCode().value());
        String token=scoped(COMPANY_A,PERMS);
        // 仅在隔离测试库注入已发生的权限版本变化，验证真实JWT过滤链拒绝旧令牌。
        jdbc.update("UPDATE sys_user SET auth_version=auth_version+1 WHERE user_name LIKE 'c04\\_%'");
        var denied=call("POST","/api/admin/prices",body(COMPANY_A,"TEST"),token);
        assertTrue(denied.getStatusCode().value()==401 || ViJson.tree(denied.getBody()).path("code").asInt()==401,denied.getBody());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM vi_price_rule",Integer.class));
    }
    @Test void 发布写入中断使替换审计及发件箱整体回滚() {
        String root=rootToken(),reviewer=scoped(COMPANY_A,PERMS);
        JsonNode old=publish(approve(submit(create(body(COMPANY_A,"TEST"),root),root),reviewer),root);
        var input=body(COMPANY_A,"TEST");input.put("sourceRuleId",old.get("ruleId").asText());
        JsonNode next=approve(submit(create(input,root),root),reviewer);
        int events=jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action!='PRICE_REJECTED'",Integer.class);
        int out=jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox",Integer.class);
        String trigger="c04_fault_"+UUID.randomUUID().toString().replace("-","");
        // 仅当前夹具的已校验专属库；临时触发器故障发生在停用旧版及发布新版之后。
        jdbc.execute("CREATE TRIGGER "+trigger+" BEFORE INSERT ON vi_outbox FOR EACH ROW BEGIN IF NEW.event_type='PRICE_PUBLISH' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='C04_TEST_ROLLBACK'; END IF; END");
        try {
            var request=action(next);request.put("replacesRuleId",old.get("ruleId").asText());request.put("replacesExpectedVersion",old.get("version").asLong());
            var result=call("POST","/api/admin/prices/"+next.get("ruleId").asText()+"/publish",request,root);
            assertEquals(500,result.getStatusCode().value());assertFalse(result.getBody().contains("C04_TEST_ROLLBACK"));
            assertEquals("PUBLISHED",jdbc.queryForObject("SELECT status FROM vi_price_rule WHERE id=?",String.class,old.get("ruleId").asText()));
            assertEquals("APPROVED",jdbc.queryForObject("SELECT status FROM vi_price_rule WHERE id=?",String.class,next.get("ruleId").asText()));
            assertEquals(events,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action!='PRICE_REJECTED'",Integer.class));
            assertEquals(out,jdbc.queryForObject("SELECT COUNT(*) FROM vi_outbox",Integer.class));
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM vi_event_log WHERE action='PRICE_REJECTED'",Integer.class));
        } finally {jdbc.execute("DROP TRIGGER "+trigger);}
    }
    @Test void 相邻未来排期在边界切换且不提前停用旧版本() {
        String root=rootToken(),reviewer=scoped(COMPANY_A,PERMS);long station=station(COMPANY_A);
        java.time.Instant boundary=java.time.Instant.now().plusSeconds(3600).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
        var first=body(COMPANY_A,"TEST");first.put("effectiveTo",boundary.toString());
        JsonNode old=publish(approve(submit(create(first,root),root),reviewer),root);
        var next=body(COMPANY_A,"TEST");next.put("sourceRuleId",old.get("ruleId").asText());next.put("effectiveFrom",boundary.toString());
        JsonNode row=publish(approve(submit(create(next,root),root),reviewer),root);
        assertEquals(old.get("ruleId").asText(),pricing.quoteForStation(station,"C04_CAR","C04_ANNUAL",boundary.minusMillis(1),0).ruleId());
        assertEquals(row.get("ruleId").asText(),pricing.quoteForStation(station,"C04_CAR","C04_ANNUAL",boundary,0).ruleId());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM vi_price_rule WHERE status='PUBLISHED'",Integer.class));
    }

}

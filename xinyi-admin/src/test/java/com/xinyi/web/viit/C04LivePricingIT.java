package com.xinyi.web.viit;

import com.xinyi.RuoYiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

/** 在隔离库显式启用LIVE计价模式验证环境隔离；不连接真实支付或外部供应商。 */
@SpringBootTest(classes=RuoYiApplication.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
        "spring.datasource.druid.master.url=jdbc:mysql://localhost:3306/xinyi_vi_it_e07?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
        "vi.environment=integration","vi.pricing.environment=LIVE","vi.providers.mode=WECHAT","vi.outbox-relay-enabled=false","server.address=127.0.0.1"})
@ActiveProfiles("druid")
class C04LivePricingIT extends PricingHttpFixture {
    @Test void LIVE只匹配LIVE且停用后不回退TEST价格() {
        String root=rootToken(),reviewer=scoped(COMPANY_A,PERMS);long station=station(SITE_DEPT);
        assertEquals(422,call("POST","/api/admin/prices",body(COMPANY_A,"TEST"),root).getStatusCode().value());
        var row=publish(approve(submit(create(body(COMPANY_A,"LIVE"),root),root),reviewer),root);
        String liveId=row.get("ruleId").asText();
        // 独立TEST记录模拟同库已有测试配置，不改LIVE版本的审核内容。
        jdbc.update("INSERT INTO vi_price_rule (owner_dept_id,environment,rule_key,revision,vehicle_type_code,inspection_type_code,status,config_json,effective_from,effective_to,created_by,submitted_by,approved_by,config_hash,approved_hash,published_at) SELECT owner_dept_id,'TEST','C04_cross_env',1,vehicle_type_code,inspection_type_code,status,config_json,effective_from,effective_to,created_by,submitted_by,approved_by,config_hash,approved_hash,published_at FROM vi_price_rule WHERE id=?",liveId);
        assertEquals(liveId,data(call("POST","/api/admin/prices/trial",match(station,1001),root)).get("ruleId").asText());
        data(call("POST","/api/admin/prices/"+liveId+"/disable",action(row),root));
        assertEquals(422,call("POST","/api/admin/prices/trial",match(station,1001),root).getStatusCode().value());
    }
}

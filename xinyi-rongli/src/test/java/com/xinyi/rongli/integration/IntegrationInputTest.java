package com.xinyi.rongli.integration;
import com.xinyi.rongli.integration.service.IntegrationInput;
import com.xinyi.rongli.platform.error.ViException;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class IntegrationInputTest {
 private final IntegrationInput input=new IntegrationInput();
 @Test void versionMustFitDeclaredInt64AndRemainNonnegative(){var body=Map.of("revisionId","1","expectedVersion",new java.math.BigInteger("9223372036854775808"),"level","LOCAL");assertThrows(ViException.class,()->input.validate("IntegrationCheckRequest",body));}
 @Test void whitelistRejectsAmbiguousRecipientAndInternalScope(){assertThrows(ViException.class,()->input.validate("IntegrationTestRecipientInput",Map.of("name","测试","kind","PHONE","ownerDeptId","1","environment","TEST","reason","测试号码","phone","13900000000","accountId","1")));assertThrows(ViException.class,()->input.validate("IntegrationProfileCreate",Map.of("kind","TENCENT_SMS","name","测试","scope",Map.of("ownerDeptId","1","environment","TEST","params",Map.of("dataScope","")))));}
 @Test void unknownContractCannotBecomePermissive(){assertThrows(IllegalStateException.class,()->input.validate("missing-schema",Map.of()));}
}

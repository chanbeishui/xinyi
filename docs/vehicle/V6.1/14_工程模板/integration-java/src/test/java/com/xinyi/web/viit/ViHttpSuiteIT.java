package com.xinyi.web.viit;
import com.xinyi.RuoYiApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
/** 真正启动嵌入式服务器再调用pytest；不是MockMvc，也不是原型页面测试。 */
@SpringBootTest(classes=RuoYiApplication.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
 properties={"vi.environment=integration","vi.providers.mode=SIMULATOR","server.address=127.0.0.1"})
@ActiveProfiles({"druid","vi-it"})
@Import(ViIntegrationTestConfiguration.class)
public class ViHttpSuiteIT {
 @LocalServerPort int port;
 @Test void realHttpAndDatabaseSuite() throws Exception {
  Path repo=Path.of(System.getenv().getOrDefault("VI_REPO_ROOT",".." )).toAbsolutePath().normalize();
  Path tests=repo.resolve("docs/vehicle/V6.0/13_真实集成测试/live");
  assertTrue(Files.isDirectory(tests),"缺真实测试程序路径");
  Path report=repo.resolve("docs/implementation/vehicle/evidence/live-junit.xml");Files.createDirectories(report.getParent());
  String python=System.getenv().getOrDefault("VI_PYTHON","python");
  List<String> cmd=new ArrayList<>(List.of(python,"-m","pytest",tests.toString(),"-q","--junitxml="+report));
  String suite=System.getenv().getOrDefault("VI_PYTEST_SUITE","identity");
  if(!List.of("identity","booking","all").contains(suite))throw new IllegalArgumentException("UNKNOWN_TEST_SUITE");
  // Restrict the actual collected file, rather than @Disabled/skip to make missing code look passed.
  if(!"all".equals(suite))cmd.set(3,tests.resolve("identity".equals(suite)?"test_identity_address.py":"test_quote_first_booking.py").toString());
  ProcessBuilder pb=new ProcessBuilder(cmd).directory(repo.toFile()).inheritIO();
  pb.environment().put("VI_BASE_URL","http://127.0.0.1:"+port);
  Process p=pb.start();boolean done=p.waitFor(15,TimeUnit.MINUTES);
  if(!done)p.destroyForcibly();assertTrue(done,"真实HTTP测试超时");assertEquals(0,p.exitValue(),"真实集成失败，查看实际JUnit报告，禁止skip补通过");
  assertTrue(Files.size(report)>0,"缺实际测试报告");
 }
}

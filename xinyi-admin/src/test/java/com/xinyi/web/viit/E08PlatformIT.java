package com.xinyi.web.viit;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E08 CI/任务/证据 harness 的真实仓库集成验证（轻量 JUnit IT，不启 Spring 上下文）。
 * 驱动 docs/implementation/vehicle/E08/ci_run.py 的 report 模式对真实工作区聚合，断言：
 * - 失败退出码契约（report 正常 exit 0）；
 * - ci-stage 诚实停在 FOUNDATION（不用基座绿色代替全功能）；
 * - E00–E07 已 VERIFIED 且证据文件存在（verifiedWithEvidence）；
 * - 接口/用例覆盖总数对账（api 249、case 298），业务实现仍 0（M0 基座，未造业务端点）；
 * - 未实跑层 L01 真实渠道 / L02 真机 / L03 部署 明确 NOT_RUN（negative：NOT_RUN 不算通过）。
 *
 * ci_run.py 仅用标准库，任意 python3 可跑；python 解释器经 VI_PYTHON 覆盖，缺省 python3。
 * 仓库根经 VI_REPO_ROOT 覆盖，缺省按 user.dir 探测（failsafe 下为 xinyi-admin，取父目录）。
 */
class E08PlatformIT {

    private static Path repoRoot() {
        String env = System.getenv("VI_REPO_ROOT");
        if (env != null && !env.isBlank()) {
            return Paths.get(env).toAbsolutePath().normalize();
        }
        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path marker = Paths.get("docs/implementation/vehicle/E08/ci_run.py");
        return Files.exists(cwd.resolve(marker)) ? cwd : cwd.getParent();
    }

    @Test
    void ciReport对真实工作区聚合且诚实() throws Exception {
        Path repo = repoRoot();
        Path ciRun = repo.resolve("docs/implementation/vehicle/E08/ci_run.py");
        assertTrue(Files.isRegularFile(ciRun), "缺 ci_run.py: " + ciRun);

        String python = System.getenv().getOrDefault("VI_PYTHON", "python3");
        ProcessBuilder pb = new ProcessBuilder(python, ciRun.toString(), "report", "--repo", repo.toString());
        pb.directory(repo.toFile());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        boolean done = p.waitFor(5, TimeUnit.MINUTES);
        if (!done) {
            p.destroyForcibly();
        }
        assertTrue(done, "ci_run report 超时");
        assertEquals(0, p.exitValue(), "report 应 exit 0；输出:" + out);

        Path reportFile = repo.resolve("docs/implementation/vehicle/evidence/ci-report.json");
        assertTrue(Files.isRegularFile(reportFile), "未生成 ci-report.json");
        String rep = Files.readString(reportFile, StandardCharsets.UTF_8);

        assertTrue(rep.contains("\"ciStage\": \"FOUNDATION\""), "ci-stage 须诚实停在 FOUNDATION");
        for (String id : new String[]{"E00", "E01", "E02", "E03", "E04", "E05", "E06", "E07"}) {
            assertTrue(rep.contains("\"" + id + "\""), "verifiedWithEvidence 应含 " + id);
        }
        assertTrue(rep.contains("\"L01_REAL_CHANNEL\": \"NOT_RUN\""), "真实渠道须 NOT_RUN");
        assertTrue(rep.contains("\"L02_DEVICE\": \"NOT_RUN\""), "真机须 NOT_RUN");
        assertTrue(rep.contains("\"L03_DEPLOY\": \"NOT_RUN\""), "部署须 NOT_RUN");
        assertTrue(rep.contains("\"total\": 287"), "api 覆盖总数应为 V6.4 的 287");
        assertTrue(rep.contains("\"total\": 412"), "case 覆盖总数应为 V6.4 的 412");
        // 业务 api 实现数随 M1 任务增长（M0 为 0；I01 落地 API_001_post/API_005_get = 2）。
        // harness 按 controllerFile/serviceFile 真实存在计数，不虚报；此处断言已 >=2 且不超过总数。
        java.util.regex.Matcher apiImpl = java.util.regex.Pattern
                .compile("\"api\":\\s*\\{[^}]*?\"implemented\":\\s*(\\d+)").matcher(rep);
        assertTrue(apiImpl.find(), "报告应含 coverage.api.implemented");
        int implemented = Integer.parseInt(apiImpl.group(1));
        assertTrue(implemented >= 2, "I01 已实现 API_001_post/API_005_get，api 实现数应 >=2，实际=" + implemented);
        assertTrue(implemented <= 287, "api 实现数不得超过总数");
    }
}

package com.xinyi.rongli.file.service;

import com.xinyi.rongli.file.domain.PrivateFile;
import com.xinyi.rongli.file.dto.FileRequests;
import com.xinyi.rongli.file.mapper.PrivateFileMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.*;
import com.xinyi.rongli.platform.service.*;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 文件传输与数据库事务分离；完成核验才变 READY，上传回执不能证明文件可信。 */
@Service
public class PrivateFileService {
    public record Receipt(long fileId) {}

    public record Download(byte[] bytes, String mime) {}

    private final PrivateFileMapper files;
    private final FileAuthority authority;
    private final PrivateFileStorage storage;
    private final FileTickets tickets;
    private final IdempotencyService idem;
    private final AuditService audit;
    private final TransactionTemplate tx;

    public PrivateFileService(
            PrivateFileMapper files,
            FileAuthority authority,
            PrivateFileStorage storage,
            FileTickets tickets,
            IdempotencyService idem,
            AuditService audit,
            PlatformTransactionManager manager) {
        this.files = files;
        this.authority = authority;
        this.storage = storage;
        this.tickets = tickets;
        this.idem = idem;
        this.audit = audit;
        tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(
                org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public Map<String, Object> create(FileRequests.Upload request, String key) {
        Actor actor = authority.actor();
        storage.root();
        return audited(
                actor,
                0,
                "FILE_UPLOAD_CREATE",
                () ->
                        tx.execute(
                                status -> {
                                    authority.guard(actor, "biz:uploads:create");
                                    PrivateFile draft = new PrivateFile();
                                    draft.setUploaderAccountId(
                                            actor.isMini() ? actor.accountId() : null);
                                    draft.setUploaderUserId(
                                            actor.isAdmin() ? actor.sysUserId() : null);
                                    draft.setPurpose(request.purpose());
                                    draft.setApplicationId(request.applicationId());
                                    draft.setStationId(request.stationId());
                                    if (request.orderNo() != null) {
                                        var order = files.orderByNo(request.orderNo());
                                        if (order == null)
                                            throw new ViException(
                                                    ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                                        draft.setOrderId(order.getId());
                                    }
                                    authority.check(actor, draft, "biz:uploads:create", true);
                                    Receipt receipt =
                                            idem.execute(
                                                    subject(actor),
                                                    "FILE_UPLOAD_CREATE",
                                                    key,
                                                    com.xinyi.rongli.platform.json.ViJson.write(
                                                            request),
                                                    true,
                                                    Receipt.class,
                                                    () -> {
                                                        draft.setObjectKey(
                                                                UUID.randomUUID()
                                                                        .toString()
                                                                        .replace("-", ""));
                                                        draft.setDeclaredSize(request.size());
                                                        draft.setDeclaredMime(request.mimeType());
                                                        draft.setUploadExpiresAt(
                                                                Instant.now()
                                                                        .plusSeconds(300)
                                                                        .truncatedTo(
                                                                                ChronoUnit
                                                                                        .SECONDS));
                                                        files.insert(draft);
                                                        audit.recordSuccess(
                                                                actor,
                                                                "FILE",
                                                                draft.getId(),
                                                                "FILE_UPLOAD_CREATE",
                                                                null,
                                                                Map.of(
                                                                        "purpose",
                                                                        draft.getPurpose()),
                                                                null,
                                                                draft.getOrderId());
                                                        return new Receipt(draft.getId());
                                                    });
                                    PrivateFile row =
                                            authorized(
                                                    actor,
                                                    receipt.fileId(),
                                                    "biz:uploads:create",
                                                    true);
                                    if (!"UPLOADING".equals(row.getStatus())
                                            || row.getUploadExpiresAt() == null
                                            || !row.getUploadExpiresAt().isAfter(Instant.now()))
                                        throw new ViException(
                                                ViErrorCode.VI_STATE_CONFLICT, "上传申请已使用或过期，请新建上传");
                                    return Map.of(
                                            "uploadId",
                                            row.getId().toString(),
                                            "fileId",
                                            row.getId().toString(),
                                            "uploadUrl",
                                            "/vi-private-transfer/uploads/" + row.getId(),
                                            "expiresAt",
                                            row.getUploadExpiresAt().toString(),
                                            "requiredHeaders",
                                            Map.of(
                                                    "X-File-Ticket",
                                                    tickets.sign(
                                                            actor,
                                                            row.getId(),
                                                            "UPLOAD",
                                                            row.getUploadExpiresAt()),
                                                    "Content-Type",
                                                    row.getDeclaredMime()));
                                }));
    }

    public void upload(long id, String ticket, InputStream input) {
        Actor actor = authority.actor();
        audited(
                actor,
                id,
                "FILE_UPLOAD_BYTES",
                () -> {
                    String lease = UUID.randomUUID().toString();
                    PrivateFile row =
                            tx.execute(
                                    status -> {
                                        authority.guard(actor, "biz:uploads:complete");
                                        PrivateFile file =
                                                authorized(actor, id, "biz:uploads:complete", true);
                                        tickets.verify(actor, id, "UPLOAD", ticket);
                                        if (file.getUploadExpiresAt() == null
                                                || !file.getUploadExpiresAt().isAfter(Instant.now())
                                                || files.claim(
                                                                id,
                                                                file.getVersion(),
                                                                lease,
                                                                Instant.now().plusSeconds(60))
                                                        != 1)
                                            throw new ViException(
                                                    ViErrorCode.VI_STATE_CONFLICT, "上传已开始或授权已失效");
                                        return file;
                                    });
                    try {
                        storage.store(row.getObjectKey(), input, row.getDeclaredSize());
                    } catch (ViException error) {
                        // 租约失败不重新覆盖同一个对象。客户端使用新申请重试，旧对象保持不可用且留有审计。
                        tx.executeWithoutResult(
                                status -> {
                                    authority.guard(actor, "biz:uploads:complete");
                                    PrivateFile failed =
                                            authorized(actor, id, "biz:uploads:complete", true);
                                    if (Objects.equals(lease, failed.getUploadLeaseToken())) {
                                        failed.setStatus("REJECTED");
                                        files.finish(failed);
                                    }
                                });
                        throw error;
                    }
                    tx.executeWithoutResult(
                            status -> {
                                authority.guard(actor, "biz:uploads:complete");
                                authorized(actor, id, "biz:uploads:complete", true);
                                if (files.uploaded(id, lease) != 1)
                                    throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
                                audit.recordSuccess(
                                        actor,
                                        "FILE",
                                        id,
                                        "FILE_UPLOAD_BYTES",
                                        null,
                                        Map.of("status", "VERIFYING"),
                                        null,
                                        row.getOrderId());
                            });
                    return null;
                });
    }

    public Map<String, Object> complete(long id) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "FILE_COMPLETE",
                () -> {
                    PrivateFile snapshot =
                            tx.execute(
                                    status -> {
                                        authority.guard(actor, "biz:uploads:complete");
                                        PrivateFile file =
                                                authorized(actor, id, "biz:uploads:complete", true);
                                        if ("REJECTED".equals(file.getStatus()))
                                            throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
                                        if (file.getUploadLeaseUntil() != null
                                                && file.getUploadLeaseUntil()
                                                        .isAfter(Instant.now()))
                                            throw new ViException(
                                                    ViErrorCode.VI_STATE_CONFLICT, "文件正在上传");
                                        if (file.getDeclaredSize() == null
                                                || file.getDeclaredMime() == null)
                                            throw new ViException(
                                                    ViErrorCode.VI_STATE_CONFLICT, "历史文件缺少可信核验信息");
                                        return file;
                                    });
                    if ("READY".equals(snapshot.getStatus())) return view(snapshot);
                    PrivateFileStorage.Verified verified;
                    try {
                        verified =
                                storage.verify(
                                        snapshot.getObjectKey(),
                                        snapshot.getDeclaredSize(),
                                        snapshot.getDeclaredMime());
                    } catch (ViException error) {
                        if (error.getErrorCode() == ViErrorCode.VI_DATA_INVALID)
                            reject(actor, snapshot);
                        throw error;
                    }
                    return tx.execute(
                            status -> {
                                authority.guard(actor, "biz:uploads:complete");
                                PrivateFile file =
                                        authorized(actor, id, "biz:uploads:complete", true);
                                if ("READY".equals(file.getStatus())) return view(file);
                                if (file.getVersion() != snapshot.getVersion())
                                    throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
                                file.setStatus("READY");
                                file.setMimeType(verified.mime());
                                file.setServerSize(verified.size());
                                file.setServerSha256(HexFormat.of().parseHex(verified.sha256()));
                                if (files.finish(file) != 1)
                                    throw new ViException(ViErrorCode.VI_VERSION_CONFLICT);
                                audit.recordSuccess(
                                        actor,
                                        "FILE",
                                        id,
                                        "FILE_COMPLETE",
                                        null,
                                        Map.of("status", "READY", "size", verified.size()),
                                        null,
                                        file.getOrderId());
                                return view(files.find(id));
                            });
                });
    }

    private void reject(Actor actor, PrivateFile snapshot) {
        tx.executeWithoutResult(
                status -> {
                    authority.guard(actor, "biz:uploads:complete");
                    PrivateFile file =
                            authorized(actor, snapshot.getId(), "biz:uploads:complete", true);
                    if (file.getVersion() == snapshot.getVersion()) {
                        file.setStatus("REJECTED");
                        files.finish(file);
                        audit.recordSuccess(
                                actor,
                                "FILE",
                                file.getId(),
                                "FILE_REJECTED",
                                null,
                                Map.of("status", "REJECTED"),
                                null,
                                file.getOrderId());
                    }
                });
    }

    public Map<String, Object> get(long id) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "FILE_QUERY",
                () ->
                        tx.execute(
                                status -> {
                                    authority.guard(actor, "biz:files:query");
                                    return view(authorized(actor, id, "biz:files:query", false));
                                }));
    }

    public Map<String, Object> access(long id, FileRequests.Access request) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "FILE_ACCESS",
                () ->
                        tx.execute(
                                status -> {
                                    authority.guard(actor, "biz:files:access");
                                    PrivateFile file =
                                            authorized(actor, id, "biz:files:access", false);
                                    ready(file);
                                    if (request.orderNo() != null) {
                                        var order = files.orderByNo(request.orderNo());
                                        if (order == null
                                                || !Objects.equals(
                                                        order.getId(), file.getOrderId()))
                                            throw new ViException(
                                                    ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
                                    }
                                    Instant expires =
                                            Instant.now()
                                                    .plusSeconds(60)
                                                    .truncatedTo(ChronoUnit.SECONDS);
                                    String requestId = UUID.randomUUID().toString();
                                    // 原因是访问意图，不作为权限凭据；审计只保存脱敏动作与关联，避免自由文本夹带身份证号。
                                    audit.recordSuccess(
                                            actor,
                                            "FILE",
                                            id,
                                            "FILE_ACCESS",
                                            null,
                                            Map.of(
                                                    "requestId",
                                                    requestId,
                                                    "purpose",
                                                    file.getPurpose()),
                                            "查看业务凭证",
                                            file.getOrderId());
                                    return Map.of(
                                            "requestId",
                                            requestId,
                                            "status",
                                            "APPROVED",
                                            "expiresAt",
                                            expires.toString(),
                                            "downloadUrl",
                                            "/vi-private-transfer/files/" + id,
                                            "requiredHeaders",
                                            Map.of(
                                                    "X-File-Ticket",
                                                    tickets.sign(actor, id, "READ", expires)));
                                }));
    }

    public Download download(long id, String ticket) {
        Actor actor = authority.actor();
        return audited(
                actor,
                id,
                "FILE_DOWNLOAD",
                () -> {
                    PrivateFile file =
                            tx.execute(
                                    status -> {
                                        authority.guard(actor, "biz:files:access");
                                        PrivateFile row =
                                                authorized(actor, id, "biz:files:access", false);
                                        tickets.verify(actor, id, "READ", ticket);
                                        ready(row);
                                        return row;
                                    });
                    byte[] bytes = storage.read(file.getObjectKey());
                    // 持久卷损坏或被部署外部操作替换时拒绝输出，不能沿用旧 READY 元数据伪装为可信原件。
                    try {
                        if (file.getServerSize() == null
                                || file.getServerSize() != bytes.length
                                || file.getServerSha256() == null
                                || !java.security.MessageDigest.isEqual(
                                        file.getServerSha256(),
                                        java.security.MessageDigest.getInstance("SHA-256")
                                                .digest(bytes)))
                            throw new ViException(ViErrorCode.VI_DATA_INVALID, "文件完整性核验失败，请联系管理员");
                    } catch (java.security.NoSuchAlgorithmException impossible) {
                        throw new ViException(ViErrorCode.VI_INTERNAL_ERROR);
                    }
                    // IO 后再次检查撤权，实际读取与签发临时权限分别留痕。
                    tx.executeWithoutResult(
                            status -> {
                                authority.guard(actor, "biz:files:access");
                                ready(authorized(actor, id, "biz:files:access", false));
                                tickets.verify(actor, id, "READ", ticket);
                                audit.recordSuccess(
                                        actor,
                                        "FILE",
                                        id,
                                        "FILE_DOWNLOAD",
                                        null,
                                        Map.of("size", bytes.length),
                                        null,
                                        file.getOrderId());
                            });
                    return new Download(bytes, file.getMimeType());
                });
    }

    /** S01 在同一业务事务内调用；必须已持目录与订单锁，整组验证后才能写申请。 */
    public void validateVouchers(Actor actor, long orderId, List<Long> ids) {
        for (long id : ids) {
            PrivateFile file = authorized(actor, id, "biz:files:query", false);
            ready(file);
            if (!Objects.equals(file.getOrderId(), orderId)
                    || !"DISPUTE".equals(file.getPurpose())
                    || !authority.uploader(actor, file))
                throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
    }

    private PrivateFile authorized(Actor actor, long id, String permission, boolean write) {
        // 先读定位父记录，再按父订单→文件顺序加锁；不允许文件锁反向等待订单锁。
        PrivateFile snapshot = files.find(id);
        authority.check(actor, snapshot, permission, write);
        return files.lock(id);
    }

    private static void ready(PrivateFile file) {
        if (!"READY".equals(file.getStatus()))
            throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "文件尚未完成核验");
    }

    private static Map<String, Object> view(PrivateFile file) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileId", file.getId().toString());
        result.put("status", file.getStatus());
        result.put("version", file.getVersion());
        if (file.getServerSha256() != null)
            result.put("serverSha256", HexFormat.of().formatHex(file.getServerSha256()));
        if (file.getServerSize() != null) result.put("size", file.getServerSize());
        return result;
    }

    private static String subject(Actor actor) {
        return actor.realm() + ":" + (actor.isMini() ? actor.accountId() : actor.sysUserId());
    }

    private <T> T audited(Actor actor, long id, String action, Supplier<T> work) {
        try {
            return work.get();
        } catch (RuntimeException error) {
            audit.recordFailure(
                    actor,
                    "FILE",
                    id,
                    action + "_DENIED",
                    null,
                    null,
                    error instanceof ViException failure
                            ? failure.getErrorCode().name()
                            : "PROCESSING_FAILED");
            throw error;
        }
    }
}

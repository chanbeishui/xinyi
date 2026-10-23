package com.xinyi.rongli.controller.admin;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.dto.DriverDtos;
import com.xinyi.rongli.service.AdminService;
import com.xinyi.rongli.service.EventService;
import com.xinyi.rongli.service.FulfillService;
import com.xinyi.rongli.service.MediaService;

/**
 * 后台收车与检测待办（B07 / B08 / B10 / B11）。
 *
 * <p>这些接口是管理端的复用入口，不是第二套履约实现：收车确认最终走
 * {@link FulfillService#confirmHandoverByAdmin}，与小程序端共用同一套交接核验与状态推进，
 * 只是把操作来源记为 ADMIN 并留痕操作人。</p>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminFulfillController
{
    @Autowired
    private AdminService adminService;

    @Autowired
    private FulfillService fulfillService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private EventService eventService;

    /** B07 待收车看板 */
    @GetMapping("/receiving/pending")
    @PreAuthorize("@ss.hasPermi('biz:receive:list')")
    public Map<String, Object> receivingPending(@RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.receivingBoard(stationId, keyword, pageNum, pageSize);
    }

    /** B07 单笔收车详情：司机、节点2证据、站点侧交接码状态 */
    @GetMapping("/receiving/{orderNo}")
    @PreAuthorize("@ss.hasPermi('biz:receive:list')")
    public Map<String, Object> receivingDetail(@PathVariable String orderNo)
    {
        return fulfillService.adminStationHandover(orderNo);
    }

    /** B07 确认收车：必须携带司机给出的 4 位交接码，核验规则与代驾端完全一致 */
    @PostMapping("/receiving/{orderNo}/handovers/{handoverId}/confirm")
    @PreAuthorize("@ss.hasPermi('biz:receive:edit')")
    public Map<String, Object> confirmReceiving(@PathVariable String orderNo, @PathVariable Long handoverId,
            @RequestBody DriverDtos.HandoverConfirmReq req)
    {
        return fulfillService.confirmHandoverByAdmin(orderNo, handoverId, req, SecurityUtils.getUserId());
    }

    /** B10 检测结果待办 */
    @GetMapping("/inspection/todos")
    @PreAuthorize("@ss.hasPermi('biz:result:list')")
    public Map<String, Object> inspectionTodos(@RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String bucket,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        return adminService.inspectionTodoBoard(stationId, keyword, bucket, pageNum, pageSize);
    }

    /**
     * B08 证件原图单独授权：后台读取敏感影像必须单独授权位，且每次查看写入审计留痕。
     *
     * <p>与小程序端的 {@code /api/common/files/{id}/content} 区别在于这里不做「同单参与方」
     * 判定（后台不是当事方），改用独立的 {@code biz:sensitive:access} 权限 + 留痕来兜底。</p>
     */
    @GetMapping("/files/{fileId}/content")
    @PreAuthorize("@ss.hasPermi('biz:sensitive:access')")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> sensitiveFile(
            @PathVariable Long fileId)
    {
        java.nio.file.Path path = mediaService.resolveContent(fileId);
        // 先留痕再返回字节，保证「看过」一定被记录，不依赖前端埋点
        eventService.record("FILE", fileId, "SENSITIVE_VIEW", null, null,
                Map.of("operator", String.valueOf(SecurityUtils.getUserId())), "后台查看证件原图");
        org.springframework.core.io.Resource resource =
                new org.springframework.core.io.FileSystemResource(path);
        org.springframework.http.MediaType type = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
        try
        {
            String probed = java.nio.file.Files.probeContentType(path);
            if (probed != null)
            {
                type = org.springframework.http.MediaType.parseMediaType(probed);
            }
        }
        catch (Exception ignored)
        {
            // 探测失败按二进制返回，不影响可读性
        }
        return org.springframework.http.ResponseEntity.ok()
                .contentType(type)
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "no-store")
                .body(resource);
    }
}

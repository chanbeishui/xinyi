package com.xinyi.rongli.fulfill.domain;

import java.time.LocalDateTime;

/**
 * 追加式结构化结果版本
 *
 * <p>对应表 vi_inspection_result，由数据库结构生成，字段与列一一对应。</p>
 */
public class InspectionResult
{
    /** 主键。数据库AUTO_INCREMENT生成内部Long；API输出十进制字符串 */
    private Long id;

    /** 订单 */
    private Long orderId;

    /** 结果版本 */
    private Integer revision;

    /** PASS/FAIL */
    private String conclusion;

    /** 最终项目与凭证；没有实时阶段 */
    private String itemsJson;

    /** 真实检测人，验证可操作站点 */
    private Long inspectorUserId;

    /** 记录人 */
    private Long submittedBy;

    /** STATION/ADMIN服务端写入 */
    private String source;

    /** 实际检测完成时间 */
    private LocalDateTime inspectedAt;

    /** 登记时间 */
    private LocalDateTime submittedAt;

    /** 被更正替换 */
    private LocalDateTime supersededAt;

    /** 当前生效结果唯一 */
    private Long activeOrder;

    /** 创建时间，统一UTC */
    private LocalDateTime createdAt;

    /** UTC更新时间；Mapper每次修改显式SET updated_at=UTC_TIMESTAMP… */
    private LocalDateTime updatedAt;

    /** 乐观锁版本 */
    private Long version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Integer getRevision()
    {
        return revision;
    }

    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    public String getConclusion()
    {
        return conclusion;
    }

    public void setConclusion(String conclusion)
    {
        this.conclusion = conclusion;
    }

    public String getItemsJson()
    {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson)
    {
        this.itemsJson = itemsJson;
    }

    public Long getInspectorUserId()
    {
        return inspectorUserId;
    }

    public void setInspectorUserId(Long inspectorUserId)
    {
        this.inspectorUserId = inspectorUserId;
    }

    public Long getSubmittedBy()
    {
        return submittedBy;
    }

    public void setSubmittedBy(Long submittedBy)
    {
        this.submittedBy = submittedBy;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public LocalDateTime getInspectedAt()
    {
        return inspectedAt;
    }

    public void setInspectedAt(LocalDateTime inspectedAt)
    {
        this.inspectedAt = inspectedAt;
    }

    public LocalDateTime getSubmittedAt()
    {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt)
    {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getSupersededAt()
    {
        return supersededAt;
    }

    public void setSupersededAt(LocalDateTime supersededAt)
    {
        this.supersededAt = supersededAt;
    }

    public Long getActiveOrder()
    {
        return activeOrder;
    }

    public void setActiveOrder(Long activeOrder)
    {
        this.activeOrder = activeOrder;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }
}

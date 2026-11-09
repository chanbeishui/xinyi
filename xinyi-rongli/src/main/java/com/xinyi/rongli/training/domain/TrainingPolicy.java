package com.xinyi.rongli.training.domain;

import com.xinyi.rongli.core.ViRecord;

/** 培训服务内部持久化记录；不得直接作为客户端写入DTO。 */
public class TrainingPolicy extends ViRecord {
    private Long ownerDeptId;

    public Long getOwnerDeptId() {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long value) {
        ownerDeptId = value;
    }

    private Long draftRevisionId;

    public Long getDraftRevisionId() {
        return draftRevisionId;
    }

    public void setDraftRevisionId(Long value) {
        draftRevisionId = value;
    }

    private Long activeRevisionId;

    public Long getActiveRevisionId() {
        return activeRevisionId;
    }

    public void setActiveRevisionId(Long value) {
        activeRevisionId = value;
    }
}

package com.xinyi.rongli.qualification.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

/** 私有证件只读取当前提交快照的父标识，不直接暴露材料或自行推断公司。 */
public interface QualificationFileMapper {
    Map<String, Object> parent(@Param("applicationId") long applicationId);
}

package com.xinyi.rongli.common.support;

/**
 * tinyint 布尔列的类型转换。
 *
 * <p>数据库布尔语义列统一声明为 {@code tinyint}，实体字段因此是 {@code Integer}。
 * 直接把 {@code Integer} 塞进视图会让前端收到 {@code 1/0} 而不是 {@code true/false}，
 * 与改造前 {@code Row.bool()} 的输出不一致，所以对外输出一律经过本类转换。</p>
 *
 * <p>不使用实体 {@code Boolean} 字段的原因：{@code vi_evidence_group.node_no} 是 1-4、
 * {@code vi_review.score} 是 1-5，MyBatis 的 Boolean 处理会把非 0 值都当 true。</p>
 */
public final class ViBool
{
    private ViBool()
    {
    }

    /**
     * 空值与 0 视为 false，其余视为 true。
     *
     * @param value 数据库 tinyint 原值
     * @return 对外输出的布尔值
     */
    public static boolean of(Integer value)
    {
        return value != null && value != 0;
    }

    /**
     * 布尔值转为可写入 tinyint 列的整型。
     *
     * @param value 布尔值，允许为 null
     * @return 1 / 0，入参为 null 时返回 null
     */
    public static Integer toInt(Boolean value)
    {
        return value == null ? null : Integer.valueOf(value ? 1 : 0);
    }
}

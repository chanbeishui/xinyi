package com.xinyi.rongli.platform.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 仅用于 vi 包显式 DATETIME(3) 字段的 Instant↔UTC LocalDateTime 映射（ADR-03）。
 * 不全局注册、不改 sys 映射；在 mapper resultMap/参数上按列显式 typeHandler 引用。
 * 数据库列保存 UTC 分量，Java 业务时刻用 Instant。
 */
@MappedTypes(Instant.class)
public final class ViUtcInstantTypeHandler extends BaseTypeHandler<Instant> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Instant value, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, LocalDateTime.ofInstant(value, ZoneOffset.UTC), Types.TIMESTAMP);
    }

    @Override
    public Instant getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convert(rs.getObject(columnName, LocalDateTime.class));
    }

    @Override
    public Instant getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getObject(columnIndex, LocalDateTime.class));
    }

    @Override
    public Instant getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convert(cs.getObject(columnIndex, LocalDateTime.class));
    }

    private Instant convert(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}

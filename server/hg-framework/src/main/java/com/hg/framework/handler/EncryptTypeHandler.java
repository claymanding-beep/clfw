package com.hg.framework.handler;

import com.hg.common.utils.AesUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 字段级加解密 TypeHandler：写库时 AES 加密，读库时解密。
 *
 * <p>用法：实体字段标注 {@code @TableField(typeHandler = EncryptTypeHandler.class)}，
 * 并在 {@code @TableName} 上开启 {@code autoResultMap = true}，即可对业务代码零侵入地透明加解密。</p>
 *
 * <p>注意：QueryWrapper 的 {@code eq/like} 条件不会自动套用本 TypeHandler，
 * 若需按加密字段查询，须在业务层先用 {@link AesUtils#encrypt(String)} 得到密文再比对（仅支持精确匹配）。</p>
 */
@MappedTypes(String.class)
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, AesUtils.encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return AesUtils.decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return AesUtils.decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return AesUtils.decrypt(cs.getString(columnIndex));
    }
}

package com.aatroxc.wecommunity.config.mybatis;

import com.aatroxc.wecommunity.model.enums.ValueEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.Assert;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * 通用的枚举类型转换器，任意类型都支持
 *
 * @author mafei007
 * @date 2020/4/6 22:39
 */
public class EnumValueTypeHandler<E extends Enum<E> & ValueEnum<?>> extends BaseTypeHandler<E> {

    private final Class<E> type;
    private final E[] enums;

    public EnumValueTypeHandler(Class<E> type) {
        Assert.notNull(type, "Enum type argument cannot be null");

        this.type = type;
        this.enums = type.getEnumConstants();

        Assert.isTrue(this.enums != null, type.getSimpleName() + " does not represent an enum type.");
    }


    private E valueToEnum(Object val) {
        return Arrays.stream(enums)
                .filter(item -> item.getValue().equals(val))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot convert " + val + " to " + this.type.getSimpleName() + " by custom value."));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        // E parameter 一定不为 null
        // BaseTypeHandler 中对参数 E parameter 处理过，不需要我们在判断 null 的情况了

        /**
         * The given argument will be converted to
         * the corresponding SQL type before being sent to the database.
         */
        ps.setObject(i, parameter.getValue());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object val = rs.getObject(columnName);
        return this.valueToEnum(val);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object val = rs.getObject(columnIndex);
        return this.valueToEnum(val);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object val = cs.getObject(columnIndex);
        return this.valueToEnum(val);
    }
}

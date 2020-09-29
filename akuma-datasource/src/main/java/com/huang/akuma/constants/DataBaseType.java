package com.huang.akuma.constants;

/**
 * @author Huang.zh
 * @date 2020/9/29 10:06
 * @Description: 数据库类型
 */
public enum DataBaseType implements com.huang.akuma.datasource.api.DataBaseType {
    MYSQL("mysql"),SQL_SERVER("sqlserver"),ORACLE("oracle");

    private String prefix;

    DataBaseType(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }
}

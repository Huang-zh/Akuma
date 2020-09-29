package com.huang.akuma.constants;

/**
 * @author Huang.zh
 * @date 2020/9/28 15:09
 * @Description: 数据源种类
 */
public enum DataSourceType implements com.huang.akuma.datasource.api.DataSourceType {
    HIKARI("HikariCP"),C3P0("C3P0"),DRUID("Druid");

    private String name;

    DataSourceType(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return null;
    }

}

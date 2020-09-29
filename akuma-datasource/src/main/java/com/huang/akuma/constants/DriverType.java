package com.huang.akuma.constants;

/**
 * @author Huang.zh
 * @date 2020/9/29 10:21
 * @Description:
 */
public enum DriverType implements com.huang.akuma.datasource.api.DriverType {

    DRIVER_MYSQL("com.mysql.cj.jdbc.Driver"),
    DRIVER_SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    DRIVER_ORACLE("oracle.jdbc.OracleDriver");

    DriverType(String driverName) {
        this.driverName = driverName;
    }

    private String driverName;

    @Override
    public String getDriverName() {
        return driverName;
    }
}

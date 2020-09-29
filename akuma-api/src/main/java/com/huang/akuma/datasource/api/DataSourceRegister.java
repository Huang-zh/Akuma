package com.huang.akuma.datasource.api;

import com.huang.akuma.datasource.settings.DataSourceSetting;

import javax.sql.DataSource;

/**
 * @author Huang.zh
 * @date 2020/9/28 14:25
 * @Description: 数据源注册器接口抽象，定义注册行为
 */
public interface DataSourceRegister {

    /**
     * @Author Huang.zh
     * @Description 实例化数据源
     * @Date 2020/9/28 16:12
     */
    DataSource buildDataSource(DataSourceSetting dataSourceSetting);

    boolean dataSourceRegistry(DataSource dataSource);

    /**
     * @Author Huang.zh
     * @Description 检测数据源是否存活
     * @Date 2020/9/28 14:33
     */
    boolean checkDataSource(DataSource dataSource);
}

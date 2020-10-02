package com.huang.akuma.registers;

import com.huang.akuma.datasource.settings.DataSourceSetting;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Huang.zh
 * @date 2020/9/29 11:37
 * @Description: HikariCP数据源注册类
 */
@Slf4j
public class HikariCPDataSourceRegister extends AbstractDataSourceRegister {

    private Map<String,HikariDataSource> existDataSources;

    private Set<String> existDataSourceNames;

    public HikariCPDataSourceRegister() {
        existDataSourceNames = new ConcurrentSkipListSet<>();
        this.existDataSources = new ConcurrentHashMap<>();
    }

    @Override
    public DataSource buildDataSource(DataSourceSetting dataSourceSetting) {
        HikariDataSource dataSource;
        if (!existDataSourceNames.contains(dataSourceSetting.getName())){
            HikariConfig config = new HikariConfig();
            config.setUsername(dataSourceSetting.getUsername());
            config.setPassword(dataSourceSetting.getPassword());
            config.setJdbcUrl(dataSourceSetting.getUrl());
            config.setDriverClassName(judgeDriverType(dataSourceSetting.getUrl()));
            config.setPoolName(dataSourceSetting.getName());
            dataSource = new HikariDataSource(config);
        } else {
            log.info("数据源已存在，从缓存中获取名称为{}的数据源！",dataSourceSetting.getName());
            dataSource = existDataSources.get(dataSourceSetting.getName());
        }
        return dataSource;
    }

    @Override
    public DataSource dataSourceRegistry(DataSource dataSource) {
        try {
            HikariDataSource hikariDataSource = (HikariDataSource)dataSource;
            existDataSources.putIfAbsent(hikariDataSource.getPoolName(),hikariDataSource);
            existDataSourceNames.add(hikariDataSource.getPoolName());
            return hikariDataSource;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean checkDataSource(DataSource dataSource) {
        return ((HikariDataSource)dataSource).isRunning();
    }
}

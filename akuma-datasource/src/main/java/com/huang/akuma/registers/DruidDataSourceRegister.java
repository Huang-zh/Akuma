package com.huang.akuma.registers;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Huang.zh
 * @date 2020/9/28 15:37
 * @Description: Druid数据源注册类
 */
@Slf4j
public class DruidDataSourceRegister extends AbstractDataSourceRegister {

    private Map<String,DruidDataSource> existDataSources;

    private Set<String> existDataSourceNames;

    public DruidDataSourceRegister() {
        existDataSourceNames = new ConcurrentSkipListSet<>();
        this.existDataSources = new ConcurrentHashMap<>();
    }

    @Override
    public boolean checkDataSource(DataSource dataSource) {
        try(DruidPooledConnection connection =
                    ((DruidDataSource)dataSource).getConnection(3000)) {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean dataSourceRegistry(DataSource dataSource) {
        try {
            DruidDataSource druidDataSource = (DruidDataSource)dataSource;
            druidDataSource.init();
            existDataSources.putIfAbsent(druidDataSource.getName(),druidDataSource);
            existDataSourceNames.add(druidDataSource.getName());
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public DataSource buildDataSource(DataSourceSetting dataSourceSetting) {
        DruidDataSource druidDataSource;
        String name = dataSourceSetting.getName();
        if (!existDataSourceNames.contains(name)){
            druidDataSource = new DruidDataSource();
            druidDataSource.setDriverClassName(judgeDriverType(dataSourceSetting.getUrl()));
            druidDataSource.setUrl(dataSourceSetting.getUrl());
            druidDataSource.setUsername(dataSourceSetting.getUsername());
            druidDataSource.setPassword(dataSourceSetting.getPassword());
            druidDataSource.setName(dataSourceSetting.getName());
            druidDataSource.setMaxActive(Byte.MAX_VALUE);
        }else{
            log.info("数据源已存在，从缓存中获取名称为{}的数据源！",dataSourceSetting.getName());
            druidDataSource = existDataSources.get(name);
        }
        return druidDataSource;
    }

}

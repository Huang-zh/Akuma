package com.huang.akuma.datasource.api.registers;


import com.huang.akuma.datasource.settings.DataSourceSetting;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @Author huang.zh
 * @Description 动态注册Mybatis的SqlSessionFactory工具类抽象
 * @Date 10:20 2020-10-02
 **/
public interface DynamicSqlSessionFactoryRegister {

    SqlSessionFactory sqlSessionFactoryRegistry(DataSourceSetting dataSourceSetting);

    SqlSessionFactory targetSqlSessionFactory(String identity);

    SqlSessionFactory removeSqlSessionFactory(String identity);
}

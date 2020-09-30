package com.huang.akuma.datasource.api.registers;


import com.huang.akuma.datasource.settings.DataSourceSetting;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @Author huang.zh
 * @Description 动态注册ORM工具类抽象
 * @Date 09:29 2020-09-30
 **/
public interface DynamicRegister {

    /**
     * @Author huang.zh
     * @Description 核心方法，动态注册dataSource和jdbcTemplate
     * @Date 09:34 2020-09-30
     * @Param [jdbcTemplate]
     * @return
     **/
    JdbcTemplate register(DataSourceSetting dataSourceSetting);


    /**
     * @Author huang.zh
     * @Description 获取指定id对应的jdbcTemplate
     * @Date 10:28 2020-09-30
     * @Param [identity]
     * @return
     **/
    JdbcTemplate targetTemplate(String identity);


    /**
     * @Author huang.zh
     * @Description 根据id删除对应的jdbcTemplate
     * @Date 09:34 2020-09-30
     * @Param [identity]
     * @return
     **/
    JdbcTemplate removeJdbcTemplate(String identity);

    /**
     * @Author huang.zh
     * @Description 检测指定id对应的jdbcTemplate
     * @Date 09:36 2020-09-30
     * @Param [identity]
     * @return
     **/
    boolean isTemplateActive(String identity);


}

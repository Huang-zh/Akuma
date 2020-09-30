package com.huang.akuma.register;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @description: 可创建返回JSON的JdbcTemplate
 * @author: huang.zh
 * @create: 2020-09-30 15:29
 **/
public class JsonRowMapperJdbcTemplateRegister extends AbstractDynamicJdbcTemplateRegister{

    @Override
    protected JdbcTemplate customJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
//        jdbcTemplate.query()
        return null;
    }
}

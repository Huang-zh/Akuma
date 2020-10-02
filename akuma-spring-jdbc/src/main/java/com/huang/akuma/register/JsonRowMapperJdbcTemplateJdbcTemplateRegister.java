package com.huang.akuma.register;

import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.custom.JsonRowMapperJdbcTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @description: 可创建返回JSON的JdbcTemplate
 * @author: huang.zh
 * @create: 2020-09-30 15:29
 **/
public class JsonRowMapperJdbcTemplateJdbcTemplateRegister extends AbstractDynamicJdbcTemplateJdbcTemplateRegister {

    public JsonRowMapperJdbcTemplateJdbcTemplateRegister(DataSourceType dataSourceType) {
        super(dataSourceType);
    }

    @Override
    protected JdbcTemplate customJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JsonRowMapperJdbcTemplate(true);
        return jdbcTemplate;
    }


}

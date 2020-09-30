package com.huang.akuma.custom;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huang.akuma.constants.TemplateConstants;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * @description:返回json的
 * @author: huang.zh
 * @create: 2020-09-30 15:38
 **/
public class JsonRowMapperJdbcTemplate extends JdbcTemplate {

    private boolean lowerCase;




    public JsonRowMapperJdbcTemplate(boolean lowerCase) {
        this.lowerCase = lowerCase;
    }

    protected static class JsonRowMapper implements RowMapper<ObjectNode>{

        private static ObjectMapper mapper;

        public JsonRowMapper() {
            mapper = new ObjectMapper();
            //格式化json
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            //总是序列化json
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            //反序列化时，未知类型的属性防止报错
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //序列化value为null的对象时防止报错
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            //修改序列化后日期格式
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.setDateFormat(new SimpleDateFormat(TemplateConstants.DEFAULT_JSON_DATE_TIME_FORMAT));
        }

        @Override
        public ObjectNode mapRow(ResultSet resultSet, int i) throws SQLException {
            while (resultSet.next()){
                ObjectNode objectNode = mapper.createObjectNode();
                // TODO: 2020-09-30 使用jackson将resultSet转化为json 考虑这个连接下的第一个热门答案 https://www.javaroad.cn/articles/2192
            }
            return null;
        }
    }


}

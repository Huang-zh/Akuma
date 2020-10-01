package com.huang.akuma.custom;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.UTF8JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huang.akuma.constants.TemplateConstants;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description:返回json的
 * @author: huang.zh
 * @create: 2020-09-30 15:38
 **/
public class JsonRowMapperJdbcTemplate extends JdbcTemplate {

    private boolean ignoreLowerCase;

    private JsonRowMapper mapper;


    public JsonRowMapperJdbcTemplate(boolean ignoreLowerCase) {
        this.ignoreLowerCase = ignoreLowerCase;
        mapper = new JsonRowMapper(ignoreLowerCase);
    }

    protected static class JsonRowMapper implements RowMapper<ObjectNode>{



        private static ObjectMapper mapper;

        public JsonRowMapper(boolean ignoreLowerCase) {
            //指定序列化的工具类
            SimpleModule module = new SimpleModule();
            module.addSerializer(new ResultSetSerializer());
            mapper = new ObjectMapper();
            mapper.registerModule(module);
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
            //是否忽略大小写
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, ignoreLowerCase);

        }

        @Override
        public ObjectNode mapRow(ResultSet resultSet, int i) throws SQLException {
            // TODO: 2020-09-30 使用jackson将resultSet转化为json 考虑这个连接下的第一个热门答案 https://www.javaroad.cn/articles/2192
            ObjectNode objectNode = mapper.createObjectNode();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int totalColumnCount = metaData.getColumnCount();
            String[] columnNames = new String[totalColumnCount];
            int[] columnTypes = new int[totalColumnCount];

            for (int j = 0; j < columnNames.length; j++) {
                columnNames[j] = metaData.getColumnLabel(j + 1);
                columnTypes[j] = metaData.getColumnType(j + 1);
            }

            boolean b;
            long l;
            double d;
            for (i = 0; i < columnNames.length; i++) {

                switch (columnTypes[i]) {
                    case Types.INTEGER:
                        objectNode.put(columnNames[i],resultSet.getInt(columnNames[i]));
                        break;
                    case Types.BIGINT:
                    case Types.DECIMAL:
                    case Types.NUMERIC:
                        objectNode.put(columnNames[i],resultSet.getBigDecimal(columnNames[i]));
                        break;
                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                        objectNode.put(columnNames[i],resultSet.getDouble(columnNames[i]));
                        break;

                    case Types.NVARCHAR:
                    case Types.VARCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.LONGVARCHAR:
                        objectNode.put(columnNames[i],resultSet.getString(columnNames[i]));
                        break;

                    case Types.BOOLEAN:
                    case Types.BIT:
                        objectNode.put(columnNames[i],resultSet.getBoolean(columnNames[i]));
                        break;
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        objectNode.put(columnNames[i],resultSet.getBytes(columnNames[i]));
                        break;
                    case Types.TINYINT:
                    case Types.SMALLINT:
                        objectNode.put(columnNames[i],resultSet.getShort(columnNames[i]));
                        break;
                    case Types.DATE:
                        objectNode.put(columnNames[i],new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                new Date(resultSet.getDate(columnNames[i]).getTime())
                        ));
                        break;
                    case Types.TIMESTAMP:
                        objectNode.put(columnNames[i],new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                new Date(resultSet.getTime(columnNames[i]).getTime())
                        ));
                        break;

//                        case Types.BLOB:
//                            Blob blob = resultSet.getBlob(i);
//                            objectNode.put(columnNames[i],blob.);
//                            serializerProvider.defaultSerializeValue(blob.getBinaryStream(), jsonGenerator);
//                            blob.free();
//                            break;
//
//                        case Types.CLOB:
//                            Clob clob = resultSet.getClob(i);
//                            serializerProvider.defaultSerializeValue(clob.getCharacterStream(), jsonGenerator);
//                            clob.free();
//                            break;

                    case Types.ARRAY:
                        throw new RuntimeException("ResultSetSerializer not yet implemented for SQL type ARRAY");

                    case Types.STRUCT:
                        throw new RuntimeException("ResultSetSerializer not yet implemented for SQL type STRUCT");

                    case Types.DISTINCT:
                        throw new RuntimeException("ResultSetSerializer not yet implemented for SQL type DISTINCT");

                    case Types.REF:
                        throw new RuntimeException("ResultSetSerializer not yet implemented for SQL type REF");
                }
            }

            return objectNode;
        }


        public String castObjectToJsonString(Object object) throws JsonProcessingException {
            String value = mapper.writeValueAsString(object);
            return value;
        }
    }

    public ObjectNode queryForJson(String sql,Object... args){
        ObjectNode jsonNodes= queryForObject(sql, mapper,args);
        return jsonNodes;
    }

    public List<ObjectNode> queryForJsonList(String sql, Object... args){
        List<ObjectNode> nodes = query(sql, mapper,args);
        return nodes;
    }

    public String queryForJsonString(String sql,Object... args){
        List<ObjectNode> nodes = queryForJsonList(sql, args);
        try {
            if (nodes.size() == 1){
                return mapper.castObjectToJsonString(nodes.get(0));
            } else {
                return mapper.castObjectToJsonString(nodes);
            }
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return "";
    }


    /**
     * @Author huang.zh
     * @Description //序列化工具
     * @Date 21:29 2020-10-01
     **/
    protected static class ResultSetSerializer extends JsonSerializer<ResultSet>{
        @Override
        public Class<ResultSet> handledType() {
            return ResultSet.class;
        }

        @Override
        public void serialize(ResultSet resultSet, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            try {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int totalColumnCount = metaData.getColumnCount();
                String[] columnNames = new String[totalColumnCount];
                int[] columnTypes = new int[totalColumnCount];


                for (int i = 0; i < columnNames.length; i++) {
                    columnNames[i] = metaData.getColumnLabel(i + 1);
                    columnTypes[i] = metaData.getColumnType(i + 1);
                }

                jsonGenerator.writeStartArray();


                while (resultSet.next()) {

                    boolean b;
                    long l;
                    double d;

                    jsonGenerator.writeStartObject();



                    jsonGenerator.writeEndObject();
                }

                jsonGenerator.writeEndArray();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}

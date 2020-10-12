package registers;

import com.huang.akuma.constants.DataBaseType;
import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import constants.SqlSessionFactoryConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.springframework.core.io.ClassPathResource;
import parser.Parser;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: huang.zh
 * @create: 2020-10-02 14:12
 **/
@Slf4j
public class DynamicSqlSessionFactoryRegister extends AbstractDynamicSqlSessionFactoryRegister {

    private static ConcurrentHashMap<String,SqlSessionFactory> sqlSessionFactoryMap;

    private static ReentrantLock lock;

    private static DynamicClassRegistry classRegistry;

    private static DynamicMapperRegistry mapperRegistry;

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    private static HashMap<String,MapperClassHolder> classHolderMap;

    private static final String DEFAULT_TEMPLATE_PATH = "templates\\Mapper.template";


    public DynamicSqlSessionFactoryRegister(DataSourceType dataSourceType){
        super(dataSourceType);
        sqlSessionFactoryMap = new ConcurrentHashMap<>(64);
        classHolderMap = new HashMap<>(64);
        lock = new ReentrantLock();
        classRegistry = new DynamicClassRegistry();
        mapperRegistry = new DynamicMapperRegistry();
    }

    public SqlSessionFactory sqlSessionFactoryRegistry(DataSourceSetting dataSourceSetting) {
        log.info("当前暂未指定mapper生成的路径，将使用默认路径：{}",DEFAULT_TEMPLATE_PATH);
        return sqlSessionFactoryRegistry(dataSourceSetting,DEFAULT_TEMPLATE_PATH);
    }

    @Override
    public SqlSessionFactory sqlSessionFactoryRegistry(DataSourceSetting dataSourceSetting,String templatePath) {
        lock.lock();
        String key = SqlSessionFactoryConstants.SQL_SESSION_FACTORY_PREFIX.concat(dataSourceSetting.getName());
        if (sqlSessionFactoryMap.containsKey(key)){
            return sqlSessionFactoryMap.get(key);
        } else {
            final SqlSessionFactory[] targetSqlSessionFactory = {null};
            try {
                DataSource dataSource = dataSourceRegister.dataSourceRegistry(dataSourceSetting);
                String finalTemplatePath = templatePath;
                Optional.ofNullable(dataSource).ifPresent(ds -> {
                    SqlSessionFactory sqlSessionFactory = buildSqlSessionFactory(ds);
                    Optional.ofNullable(sqlSessionFactory).ifPresent(factory -> {

                        //注册mapper
                        if (mapperRegistry(factory,dataSourceSetting.getName(),judgeDatabase(dataSourceSetting.getUrl())
                                , finalTemplatePath)){
                            targetSqlSessionFactory[0] = targetSqlSessionFactory(key);
                            sqlSessionFactoryMap.putIfAbsent(key,targetSqlSessionFactory[0]);
                        }
                    });
                });
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
            return targetSqlSessionFactory[0];
        }
    }



    @Override
    public SqlSessionFactory targetSqlSessionFactory(String identity) {
        return sqlSessionFactoryMap.get(identity);
    }

    @Override
    public SqlSessionFactory removeSqlSessionFactory(String identity) {
        return sqlSessionFactoryMap.remove(identity);
    }

    public Object executeMapperMethod(String dataSourceName,String tableName,String methodName,Class[] parameterTypes,Object[] parameters){
        AtomicReference<Object> result = new AtomicReference<>(null);
        String key = SqlSessionFactoryConstants.SQL_SESSION_FACTORY_PREFIX.concat(dataSourceName);
        SqlSessionFactory sqlSessionFactory = targetSqlSessionFactory(key);
        Optional.ofNullable(classHolderMap.get(dataSourceName)).ifPresent(mapperClassHolder -> {
            String mapperName = lineToHump(tableName).concat(SqlSessionFactoryConstants.DEFAULT_MAPPER_SUFFIX);
            if (mapperClassHolder.containsMapper(mapperName)){
                Class<?> targetMapperClass = mapperClassHolder.targetMapperClass(mapperName);
                try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE,true)){
                    Object targetMapper = sqlSession.getMapper(targetMapperClass);
                    Method load = targetMapperClass.getDeclaredMethod(methodName, parameterTypes);
                    if (methodName.equals("pageList")){
                        //分页查询需要先组装参数
                        parameters[0] = (((int)parameters[0])-1) * ((int)parameters[1]);
                    }
                    result.set(load.invoke(targetMapper, parameters));
                }catch (Exception e){
                    log.error(e.getMessage());
                }
            }
        });
        return result.get();
    }

    /**
     * @Author huang.zh
     * @Description 为当前sqlSessionFactory的数据源生成所有表对应的mapper
     * @Date 09:01 2020-10-10
     * @Param [sqlSessionFactory]
     * @return  boolean
     **/
    private boolean mapperRegistry(SqlSessionFactory sqlSessionFactory, String dataSourceName, DataBaseType dataBaseType
            ,String templatePath){
        boolean flag = false;
        try(SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE, TransactionIsolationLevel.READ_COMMITTED)) {
            String findTableSql = "";
            switch (dataBaseType){
                case MYSQL:
                    findTableSql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=(select database())";
                    break;
                case SQL_SERVER:
                    findTableSql = "SELECT Name AS TABLE_NAME FROM SysObjects WHERE XType='U'";
                    break;
                case ORACLE:
                    findTableSql = "SELECT TABLE_NAME FROM USER_TABLES ";
                    break;
            }
            ResultSet resultSet = sqlSession.getConnection().prepareStatement(findTableSql).executeQuery();
            List<Map<String, Object>> mapList = convertList(resultSet);
            String content = getFileContent(templatePath);
            List<Class> mapperClasses = new ArrayList<>();
            MapperClassHolder mapperClassHolder = new MapperClassHolder(dataSourceName);
            for (Map<String, Object> map : mapList) {
                String tableName = String.valueOf(map.get(SqlSessionFactoryConstants.DEFAULT_TABLE_NAME));
                String targetContent = content;
                targetContent = Parser.parse("@{","}",targetContent,lineToHump(tableName),tableName,tableName,tableName);
                String name = lineToHump(tableName) + SqlSessionFactoryConstants.DEFAULT_MAPPER_SUFFIX;
                Class mapperClass = classRegistry.compile(name,targetContent,SqlSessionFactoryConstants.DEFAULT_MAPPER_PACKAGE);
                mapperClassHolder.addMapperClass(name, mapperClass);
                mapperClasses.add(mapperClass);
            }
            //维护当前数据源对应的所有mapper的class
            classHolderMap.put(dataSourceName,mapperClassHolder);
            sqlSessionFactory = mapperRegistry.doMapperRegistry(mapperClasses, dataSourceName, sqlSessionFactory);
            String key = SqlSessionFactoryConstants.SQL_SESSION_FACTORY_PREFIX.concat(dataSourceName);
            removeSqlSessionFactory(key);
            sqlSessionFactoryMap.put(key,sqlSessionFactory);
            flag = true;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return flag;
    }

    private static List<Map<String, Object>> convertList(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (ResultSet resultSet = rs){
            rs = null; //help gc.
            ResultSetMetaData md = resultSet.getMetaData();
            int columnCount = md.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), resultSet.getObject(i));
                }
                list.add(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String getFileContent(String filePath) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(filePath);
        // 获得File对象，当然也可以获取输入流对象
        File file = classPathResource.getFile();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
        }
        return content.toString();
    }

    private DataBaseType judgeDatabase(String url){
        String[] array = url.split(":");
        String prefix = array[1];
        DataBaseType result = null;
        if (DataBaseType.MYSQL.getPrefix().equals(prefix)){
            result = DataBaseType.MYSQL;
        }else if (DataBaseType.SQL_SERVER.getPrefix().equals(prefix)){
            result = DataBaseType.SQL_SERVER;
        }else if (DataBaseType.ORACLE.getPrefix().equals(prefix)){
            result = DataBaseType.ORACLE;
        }
        return result;
    }

    private class MapperClassHolder{

        private String dataSourceName;

        private HashMap<String,Class<?>> classMap;

        private List<String> existMapperClasses;

        private MapperClassHolder(String dataSourceName) {
            this.dataSourceName = dataSourceName;
            classMap = new HashMap<>(64);
            existMapperClasses = new ArrayList<>();
        }

        private boolean containsMapper(String mapperClassName){
            return existMapperClasses.contains(mapperClassName);
        }

        private Class<?> addMapperClass(String mapperClassName,Class<?> mapperClass){
            Class<?> clazz = classMap.put(mapperClassName, mapperClass);
            existMapperClasses.add(mapperClassName);
            return clazz;
        }

        private Class<?> targetMapperClass(String mapperClassName){
            return classMap.get(mapperClassName);
        }
    }
}

package registers;

import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import constants.SqlSessionFactoryConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description:
 * @author: huang.zh
 * @create: 2020-10-02 14:12
 **/
@Slf4j
public class DynamicSqlSessionFactoryRegister extends AbstractDynamicSqlSessionFactoryRegister {

    private static ConcurrentHashMap<String,SqlSessionFactory> sqlSessionFactoryMap;

    private static ReentrantLock lock;


    public DynamicSqlSessionFactoryRegister(DataSourceType dataSourceType){
        super(dataSourceType);
        sqlSessionFactoryMap = new ConcurrentHashMap<>(64);
        lock = new ReentrantLock();
    }

    @Override
    public SqlSessionFactory sqlSessionFactoryRegistry(DataSourceSetting dataSourceSetting) {
        lock.lock();
        String key = SqlSessionFactoryConstants.SQL_SESSION_FACTORY_PREFIX.concat(dataSourceSetting.getName());
        if (sqlSessionFactoryMap.containsKey(key)){
            return sqlSessionFactoryMap.get(key);
        } else {
            final SqlSessionFactory[] targetSqlSessionFactory = {null};
            try {
                DataSource dataSource = dataSourceRegister.dataSourceRegistry(dataSourceSetting);
                Optional.ofNullable(dataSource).ifPresent(ds -> {
                    SqlSessionFactory sqlSessionFactory = buildSqlSessionFactory(ds);
                    Optional.ofNullable(sqlSessionFactory).ifPresent(factory -> {
                        targetSqlSessionFactory[0] = factory;
                        sqlSessionFactoryMap.putIfAbsent(key,targetSqlSessionFactory[0]);
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

    /**
     * @Author huang.zh
     * @Description 为当前sqlSessionFactory的数据源生成所有表对应的mapper
     * @Date 09:01 2020-10-10
     * @Param [sqlSessionFactory]
     * @return  boolean
     **/
    private boolean mapperRegistry(SqlSessionFactory sqlSessionFactory){
        boolean flag = false;
        try(SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE, TransactionIsolationLevel.READ_COMMITTED)) {
            ResultSet resultSet = sqlSession.getConnection()
                    .prepareStatement("select TABLE_NAME from information_schema.TABLES where TABLE_SCHEMA=(select database())")
                    .executeQuery();
            List<Map<String, Object>> mapList = convertList(resultSet);
            String content = getFileContent("templates\\Mapper.template");
            // TODO: 2020-10-10 待完成内容：遍历resultSet，生成mapper
            flag = true;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return flag;
    }

    public static List<Map<String, Object>> convertList(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (ResultSet resultSet = rs){
            rs = null; //help gc.
            ResultSetMetaData md = resultSet.getMetaData();
            int columnCount = md.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> rowData = new HashMap<String, Object>();
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

    public static String getFileContent(String filePath) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(filePath);

        // 获得File对象，当然也可以获取输入流对象
        File file = classPathResource.getFile();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
        }
        return content.toString();
    }

    @Override
    public SqlSessionFactory targetSqlSessionFactory(String identity) {
        return sqlSessionFactoryMap.get(identity);
    }

    @Override
    public SqlSessionFactory removeSqlSessionFactory(String identity) {
        return sqlSessionFactoryMap.remove(identity);
    }
}

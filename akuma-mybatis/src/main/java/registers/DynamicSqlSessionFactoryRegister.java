package registers;

import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import constants.SqlSessionFactoryConstants;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description:
 * @author: huang.zh
 * @create: 2020-10-02 14:12
 **/
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

    @Override
    public SqlSessionFactory targetSqlSessionFactory(String identity) {
        return sqlSessionFactoryMap.get(identity);
    }

    @Override
    public SqlSessionFactory removeSqlSessionFactory(String identity) {
        return sqlSessionFactoryMap.remove(identity);
    }
}

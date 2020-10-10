package registers;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Huang.zh
 * @date 2020/10/10 11:04
 * @Description: Mapper运行时动态注册工具类
 */
@Slf4j
public class DynamicMapperRegistry {

    private TransactionFactory transactionFactory;

    private SqlSessionFactoryBuilder factoryBuilder;

    public DynamicMapperRegistry() {
        transactionFactory = new JdbcTransactionFactory();
        factoryBuilder = new SqlSessionFactoryBuilder();
    }

    public SqlSessionFactory doMapperRegistry(List<Class> mapperClasses, String environmentId, SqlSessionFactory sqlSessionFactory){
        AtomicReference<SqlSessionFactory> factory = new AtomicReference<>(null);
        try {
            DataSource dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
            Optional.ofNullable(dataSource).ifPresent(ds -> {
                Environment environment = new Environment(environmentId, transactionFactory, dataSource);
                Configuration configuration = new Configuration(environment);
                for (Class mapperClass : mapperClasses) {
                    configuration.addMapper(mapperClass);
                }
                // TODO: 2020/10/10
                factory.set(factoryBuilder.build(configuration));
            });
        }catch (Exception e){
            log.error("注册Mapper到SqlSessionFactory发生异常！");
        }
        return factory.get();
    }
}

import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.Test;
import registers.DynamicSqlSessionFactoryRegister;

import java.util.List;

/**
 * @description:测试用例
 * @author: huang.zh
 * @create: 2020-10-02 14:43
 **/

public class SimpleSqlSessionFactoryTestDemo {

    DynamicSqlSessionFactoryRegister register = new DynamicSqlSessionFactoryRegister(DataSourceType.DRUID);

    @Test
    public void test(){
        DataSourceSetting sourceSetting = new DataSourceSetting("testOfShopWithDruid","jdbc:mysql://127.0.0.1:3306/shop?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
                ,"root","123456");
        SqlSessionFactory factory = register.sqlSessionFactoryRegistry(sourceSetting);
//        System.out.println(factory.getConfiguration().getCacheNames());
        try(SqlSession sqlSession = factory.openSession(ExecutorType.SIMPLE,TransactionIsolationLevel.READ_COMMITTED)) {
            JdbcTransaction transaction = new JdbcTransaction(sqlSession.getConnection());
            // TODO: 2020-10-02 无法通过当前方式直接执行sql语句，考虑引入字节码技术动态生成mapper，通过mapper代理对象进行ORM操作
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

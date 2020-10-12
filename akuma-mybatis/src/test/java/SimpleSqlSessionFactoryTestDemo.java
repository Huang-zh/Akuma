import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.junit.Test;
import registers.DynamicSqlSessionFactoryRegister;

import java.util.Collection;

/**
 * @description:测试用例
 * @author: huang.zh
 * @create: 2020-10-02 14:43
 **/
public class SimpleSqlSessionFactoryTestDemo {

    DynamicSqlSessionFactoryRegister register = new DynamicSqlSessionFactoryRegister(DataSourceType.DRUID);

    @Test
    public void test() throws Throwable {
        String dataSourceName = "testOfShopWithDruid";
        DataSourceSetting sourceSetting = new DataSourceSetting(dataSourceName,"jdbc:mysql://127.0.0.1:3306/logging-system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
                ,"root","123456");
        SqlSessionFactory factory = register.sqlSessionFactoryRegistry(sourceSetting);
        SqlSession sqlSession = factory.openSession(ExecutorType.BATCH, TransactionIsolationLevel.READ_COMMITTED);
        //获取所有mapper
        Collection<Class<?>> mappers = sqlSession.getConfiguration().getMapperRegistry().getMappers();
        //测试主键查询
        Object result = register.executeMapperMethod(dataSourceName, "operation_log", "load",
                new Class[]{int.class}, new Object[]{1});
        System.out.println(result);
        System.out.println("--------------------------");
        result = register.executeMapperMethod(dataSourceName, "operation_log", "delete",
                new Class[]{int.class}, new Object[]{5});
        System.out.println(result);
    }

}

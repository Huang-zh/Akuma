package registers;

import com.alibaba.druid.pool.DruidAbstractDataSource;
import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.registers.AbstractDataSourceRegister;
import com.huang.akuma.registers.DruidDataSourceRegister;
import com.huang.akuma.registers.HikariCPDataSourceRegister;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

/**
 * @description:
 * @author: huang.zh
 * @create: 2020-10-02 13:54
 **/
@Slf4j
public abstract class AbstractDynamicSqlSessionFactoryRegister implements com.huang.akuma.datasource.api.registers.DynamicSqlSessionFactoryRegister {


    protected static AbstractDataSourceRegister dataSourceRegister;

    public AbstractDynamicSqlSessionFactoryRegister() {

    }

    public AbstractDynamicSqlSessionFactoryRegister(DataSourceType dataSourceType){
        switch (dataSourceType){
            case DRUID:
                dataSourceRegister = new DruidDataSourceRegister();
                break;
            case HIKARI:
                dataSourceRegister = new HikariCPDataSourceRegister();
                break;
            default:
                log.error("请指定一种数据源类型");
                break;
        }
    }

    protected SqlSessionFactory buildSqlSessionFactory(DataSource dataSource){
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        String dataSourceName = "";
        if (dataSource instanceof DruidAbstractDataSource){
            dataSourceName = ((DruidAbstractDataSource)dataSource).getName();
        } else if (dataSource instanceof HikariDataSource) {
            dataSourceName = ((HikariDataSource)dataSource).getPoolName();
        }
        if (StringUtils.isBlank(dataSourceName)){
            log.error("未获取到数据源的名称！");
            return null;
        }
        Environment environment = new Environment(dataSourceName, transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        return sqlSessionFactory;
    }

    public DataSource targetDataSource(String identity){
        return dataSourceRegister.targetDataSource(identity);
    }

}

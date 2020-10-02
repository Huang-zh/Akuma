package com.huang.akuma.registers;

import com.huang.akuma.constants.DataBaseType;
import com.huang.akuma.constants.DriverType;
import com.huang.akuma.datasource.api.registers.DataSourceRegister;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Huang.zh
 * @date 2020/9/28 15:22
 * @Description: 注册数据源抽象类
 */
@Slf4j
public abstract class AbstractDataSourceRegister implements DataSourceRegister {

    private static ReentrantLock lock = new ReentrantLock();

    public AbstractDataSourceRegister() {

    }

    /**
     * @Author Huang.zh
     * @Description 注册数据源
     * @Date 2020/9/28 14:33
     */
    public DataSource dataSourceRegistry(DataSourceSetting dataSourceSetting){
        DataSource targetDataSource = null;
        if (StringUtils.isBlank(dataSourceSetting.getName())){
            log.error("请为当前添加的数据源指定一个名称！");
            return targetDataSource;
        }
        boolean flag = false;
        String dataSourceId = UUID.randomUUID().toString();
        lock.lock();
        try {
            DataSource dataSource = buildDataSource(dataSourceSetting);
            if (checkDataSource(dataSource)){
                targetDataSource = dataSourceRegistry(dataSource);
            } else {
                log.error("当前id为{}的数据源连接异常！",dataSourceId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return targetDataSource;
    }

    /**
     * @Author Huang.zh
     * @Description 通过数据库连接解析数据库驱动类型
     * @Date 2020/9/29 10:02
     */
    protected String judgeDriverType(String url){
        String[] array = url.split(":");
        String prefix = array[1];
        String driverName = "";
        if (DataBaseType.MYSQL.getPrefix().equals(prefix)){
            driverName = DriverType.DRIVER_MYSQL.getDriverName();
        }else if (DataBaseType.SQL_SERVER.getPrefix().equals(prefix)){
            driverName = DriverType.DRIVER_SQL_SERVER.getDriverName();
        }else if (DataBaseType.ORACLE.getPrefix().equals(prefix)){
            driverName = DriverType.DRIVER_ORACLE.getDriverName();
        }
        return driverName;
    }

}

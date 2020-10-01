package com.huang.akuma.register;

import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.constants.TemplateConstants;
import com.huang.akuma.datasource.api.registers.DynamicRegister;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import com.huang.akuma.registers.AbstractDataSourceRegister;
import com.huang.akuma.registers.DruidDataSourceRegister;
import com.huang.akuma.registers.HikariCPDataSourceRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description:动态注册ORM操作模版的抽象工具类
 * @author: huang.zh
 * @create: 2020-09-30 09:37
 **/
@Slf4j
public abstract class AbstractDynamicJdbcTemplateRegister implements DynamicRegister {

    private static ConcurrentHashMap<String, JdbcTemplate> templateMap;

    private static AbstractDataSourceRegister dataSourceRegister;

    private static ReentrantLock lock;


    private AbstractDynamicJdbcTemplateRegister() {
        this.templateMap = new ConcurrentHashMap<>(64);
        lock = new ReentrantLock();
    }

    public AbstractDynamicJdbcTemplateRegister(DataSourceType type){
        this();
        switch (type){
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

    @Override
    public JdbcTemplate register(DataSourceSetting dataSourceSetting) {
        lock.lock();
        try {
            DataSource dataSource = dataSourceRegister.dataSourceRegistry(dataSourceSetting);
            JdbcTemplate jdbcTemplate = customJdbcTemplate();
            Optional<JdbcTemplate> templateOptional = Optional.ofNullable(jdbcTemplate);
            templateOptional.ifPresent(template -> {
                Optional<DataSource> dataSourceOptional = Optional.ofNullable(dataSource);
                dataSourceOptional.ifPresent(ds -> {
                    template.setDatabaseProductName(dataSourceSetting.getName());
                    template.setDataSource(ds);
                });
            });
            JdbcTemplate template = templateOptional.get();
            templateMap.putIfAbsent(
                    TemplateConstants.JDBC_TEMPLATE_PREFIX.concat(dataSourceSetting.getName()),
                    template);
            return template;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public JdbcTemplate targetTemplate(String identity) {
        return templateMap.getOrDefault(identity,null);
    }

    @Override
    public JdbcTemplate removeJdbcTemplate(String identity) {
        return templateMap.remove(identity);
    }

    @Override
    public boolean isTemplateActive(String identity) {
        JdbcTemplate jdbcTemplate = targetTemplate(identity);
        DataSource dataSource = jdbcTemplate.getDataSource();
        return dataSourceRegister.checkDataSource(dataSource);
    }


    /**
     * @Author huang.zh
     * @Description 抽象方法，子类继承当前类可以通过实现该方法返回定制化的JdbcTemplate
     * @Date 10:33 2020-09-30
     * @Param []
     * @return
     **/
    // TODO: 2020-09-30 设计两个子类实现该方法，分别通过反射封装JSON和实体类
    protected abstract JdbcTemplate customJdbcTemplate();
}

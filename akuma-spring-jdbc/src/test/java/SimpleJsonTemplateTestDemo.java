import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.custom.JsonRowMapperJdbcTemplate;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import com.huang.akuma.register.AbstractDynamicJdbcTemplateRegister;
import com.huang.akuma.register.JsonRowMapperJdbcTemplateRegister;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;

/**
 * @description:
 * @author: huang.zh
 * @create: 2020-10-01 21:39
 **/
public class SimpleJsonTemplateTestDemo {

    AbstractDynamicJdbcTemplateRegister register = new JsonRowMapperJdbcTemplateRegister(DataSourceType.DRUID);

    @Test
    public void test() throws InterruptedException {
        DataSourceSetting sourceSetting = new DataSourceSetting("testOfDruid","jdbc:mysql://www.lemonhuang.com:3306/zrlog_test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
                ,"root","123456");
        JdbcTemplate jdbcTemplate = this.register.register(sourceSetting);
        System.out.println(((JsonRowMapperJdbcTemplate)jdbcTemplate).queryForJsonString("SELECT * FROM foot_slogen LIMIT 2"));
        new CountDownLatch(1).await();
    }
}

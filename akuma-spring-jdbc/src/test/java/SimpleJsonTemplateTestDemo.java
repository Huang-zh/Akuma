import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.custom.JsonRowMapperJdbcTemplate;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import com.huang.akuma.register.AbstractDynamicJdbcTemplateJdbcTemplateRegister;
import com.huang.akuma.register.JsonRowMapperJdbcTemplateJdbcTemplateRegister;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.CountDownLatch;

/**
 * @description: 测试用例
 * @author: huang.zh
 * @create: 2020-10-01 21:39
 **/
public class SimpleJsonTemplateTestDemo {

//    AbstractDynamicJdbcTemplateJdbcTemplateRegister register = new JsonRowMapperJdbcTemplateJdbcTemplateRegister(DataSourceType.DRUID);
    AbstractDynamicJdbcTemplateJdbcTemplateRegister register = new JsonRowMapperJdbcTemplateJdbcTemplateRegister(DataSourceType.HIKARI);

    @Test
    public void test() throws InterruptedException {
        DataSourceSetting sourceSetting = new DataSourceSetting("testOfDruid","jdbc:mysql://127.0.0.1:3306/shop?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
                ,"root","123456");
        JdbcTemplate jdbcTemplate = this.register.register(sourceSetting);
        System.out.println(((JsonRowMapperJdbcTemplate)jdbcTemplate).queryForJsonString("SELECT * FROM shop_product LIMIT 2"));
        new CountDownLatch(1).await();
    }
}

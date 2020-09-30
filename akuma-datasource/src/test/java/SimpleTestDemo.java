import com.huang.akuma.datasource.settings.DataSourceSetting;
import com.huang.akuma.registers.AbstractDataSourceRegister;
import com.huang.akuma.registers.DruidDataSourceRegister;
import com.huang.akuma.registers.HikariCPDataSourceRegister;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author Huang.zh
 * @date 2020/9/29 9:15
 * @Description: 单元测试
 */
public class SimpleTestDemo {
    AbstractDataSourceRegister druidDataSourceRegister = new DruidDataSourceRegister();

    AbstractDataSourceRegister hikariDataSourceRegister = new HikariCPDataSourceRegister();

    @Test
    public void test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0;i<10;i++){
            if (i%2 == 0){
                new Thread(new HikariTask()).start();
            }else{
                new Thread(new DruidTask()).start();
            }
        }
        latch.await();
    }



    public class DruidTask implements Runnable{
        @Override
        public void run() {
            DataSourceSetting sourceSetting = new DataSourceSetting("testOfDruid","jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
                    ,"root","123456");
            druidDataSourceRegister.dataSourceRegistry(sourceSetting);
        }
    }

    public class HikariTask implements Runnable{
        @Override
        public void run() {
            DataSourceSetting sourceSetting =new DataSourceSetting("testOfHikari","jdbc:mysql://localhost:3306/test" +
                    "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
                    ,"root","123456");
            hikariDataSourceRegister.dataSourceRegistry(sourceSetting);
        }
    }
}

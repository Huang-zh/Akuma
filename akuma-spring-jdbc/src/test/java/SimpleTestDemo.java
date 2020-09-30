/**
 * @description:简单的测试用例
 * @author: huang.zh
 * @create: 2020-09-30 15:23
 **/
public class SimpleTestDemo {



    public void test(){

    }

    public class dynamicJdbcTemplateRegistry implements Runnable{

        private String url;

        private String username;

        private String password;

        public dynamicJdbcTemplateRegistry(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        @Override
        public void run() {

        }
    }
}

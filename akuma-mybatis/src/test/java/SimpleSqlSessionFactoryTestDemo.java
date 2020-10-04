import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import parser.Parser;
import registers.DynamicClassRegistry;
import registers.DynamicSqlSessionFactoryRegister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:测试用例
 * @author: huang.zh
 * @create: 2020-10-02 14:43
 **/
public class SimpleSqlSessionFactoryTestDemo {

    DynamicSqlSessionFactoryRegister register = new DynamicSqlSessionFactoryRegister(DataSourceType.DRUID);

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    private DynamicClassRegistry dynamicClassRegistry = new DynamicClassRegistry();

    @Test
    public void test(){
        DataSourceSetting sourceSetting = new DataSourceSetting("testOfShopWithDruid","jdbc:mysql://127.0.0.1:3306/shop?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
                ,"root","123456");
        SqlSessionFactory factory = register.sqlSessionFactoryRegistry(sourceSetting);
        ArrayList<String> list = new ArrayList<>(factory.getConfiguration().getMappedStatementNames());
        System.out.println(list);
//        System.out.println(factory.getConfiguration().getCacheNames());
        try(SqlSession sqlSession = factory.openSession(ExecutorType.SIMPLE,TransactionIsolationLevel.READ_COMMITTED)) {
            ResultSet resultSet = sqlSession.getConnection()
                    .prepareStatement("select TABLE_NAME from information_schema.TABLES where TABLE_SCHEMA=(select database())")
                    .executeQuery();
            List<Map<String, Object>> mapList = convertList(resultSet);
            String content = getFileContent("templates\\Mapper.template");
            int i = 0;
            for (Map<String, Object> map : mapList) {
                if (i == 0){
                    String tableName = String.valueOf(map.get("TABLE_NAME"));
                    String targetContent = content;
                    targetContent = Parser.parse("${","}",targetContent,lineToHump(tableName),tableName);

                    Class interfaceImpl = Class.forName("StudentMapper");//这里要写全类名
                    Object instance = Proxy.newProxyInstance(
                            interfaceImpl.getClassLoader(),
                            new Class[]{interfaceImpl},
                            new MyInvocationHandler(sqlSession.getMapper(interfaceImpl))
                    );

                    System.out.println(targetContent);
                }
                i++;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public class MyInvocationHandler implements InvocationHandler {

        private Object target;

        public MyInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(target,args);
        }
    }


    public static List<Map<String, Object>> convertList(ResultSet rs) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (ResultSet resultSet = rs){
            rs = null; //help gc.
            ResultSetMetaData md = resultSet.getMetaData();
            int columnCount = md.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), resultSet.getObject(i));
                }
                list.add(rowData);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }


    public static String getFileContent(String filePath) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(filePath);

        // 获得File对象，当然也可以获取输入流对象
        File file = classPathResource.getFile();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
        }
        return content.toString();
    }

    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

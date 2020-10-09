import com.huang.akuma.constants.DataSourceType;
import com.huang.akuma.datasource.settings.DataSourceSetting;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import parser.Parser;
import registers.DynamicClassRegistry;
import registers.DynamicSqlSessionFactoryRegister;

import javax.sql.DataSource;
import javax.tools.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
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
    public void test() throws Throwable {
        String dataSourceName = "testOfShopWithDruid";
        DataSourceSetting sourceSetting = new DataSourceSetting(dataSourceName,"jdbc:mysql://127.0.0.1:3306/logging-system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
                ,"root","123456");
        SqlSessionFactory factory = register.sqlSessionFactoryRegistry(sourceSetting);
        DataSource dataSource = factory.getConfiguration().getEnvironment().getDataSource();
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
                    String name = lineToHump(tableName) + "Mapper";
                    Class interfaceImpl = compile(name,targetContent);//这里要写全类名
                    TransactionFactory transactionFactory = new JdbcTransactionFactory();
                    Environment environment = new Environment("development", transactionFactory, dataSource);
                    Configuration configuration = new Configuration(environment);
                    configuration.addMapper(interfaceImpl);
                    factory = new SqlSessionFactoryBuilder().build(configuration);
                    SqlSession sqlSession1 = factory.openSession();
                    Object mapper = sqlSession1.getMapper(interfaceImpl);
                    Method load = interfaceImpl.getDeclaredMethod("load", int.class);
                    Object result = load.invoke(mapper, 1);
                    System.out.println(result);
                }
                i++;
            }

        }catch (Exception e){
            e.printStackTrace();
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

    public Class<?> compile(String name, String content) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        String targetName = "mappers.".concat(name);
        StrSrcJavaObject srcObject = new StrSrcJavaObject(name, content);
        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(srcObject);
        String flag = "-d";
        String path = this.getClass().getClassLoader().getResource("").getPath();
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
        Iterable<String> options = Arrays.asList(flag, path);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, fileObjects);
        boolean result = task.call();
        if (result == true) {
            System.out.println("Compile it successfully.");
            try {
                return Class.forName(targetName);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static class StrSrcJavaObject extends SimpleJavaFileObject {
        private String content;

        public StrSrcJavaObject(String name, String content) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }
}

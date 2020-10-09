

import org.junit.Test;

import javax.tools.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;

/**
 * @author Huang.zh
 * @date 2020/10/9 10:57
 * @Description:
 */
public class CompileTest {

    public Class<?> compile(String name, String content) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        StrSrcJavaObject srcObject = new StrSrcJavaObject(name, content);
        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(srcObject);
        String flag = "-d";
        String path = this.getClass().getClassLoader().getResource("").getPath();
//        String outDir = "D:\\";
        Iterable<String> options = Arrays.asList(flag, path);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, fileObjects);
        boolean result = task.call();
        if (result == true) {
            System.out.println("Compile it successfully.");
            try {
                return Class.forName(name);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            /*ClassLoader loader = CompileTest.class.getClassLoader();
            Class<?> cls;
            try
            {
                cls = loader.loadClass(name);
                return cls;
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }*/
        }

        return null;
    }

    @Test
    public void test (){
        Class<?> cls = compile("Test",
                " public class Test{ public static void main(String[] args){System.out.println(\"compile test.\");} }");
        System.out.println(cls.getCanonicalName());
        try {
            Method method = cls.getMethod("main", String[].class);
            System.out.println(method.getName());
            method.invoke(null, new Object[]{new String[]{}});
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

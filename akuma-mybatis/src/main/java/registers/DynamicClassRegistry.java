package registers;

import lombok.extern.slf4j.Slf4j;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;

/**
 * @description:类注册器，用于运行时通知JVM动态加载Class
 * @author: huang.zh
 * @create: 2020-10-04 15:26
 **/
@Slf4j
class DynamicClassRegistry {

    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public Class<?> compile(String name, String content,String packageName) {
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        if (!packageName.endsWith(".")){
            packageName = packageName.concat(".");
        }
        String targetName = packageName.concat(name);
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
            log.info("Compile class successfully.classPath:{}",targetName);
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

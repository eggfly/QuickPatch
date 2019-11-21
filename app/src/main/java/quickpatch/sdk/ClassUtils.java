package quickpatch.sdk;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexFile;

public class ClassUtils {
    /**
     * 使用Array.newInstance间接获取函数参数，只在函数名相同的情况下调用，性能待测试
     *
     * @param method
     * @return
     */
    public static String getSignature(Method method) {
        String sig;
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> c : method.getParameterTypes())
            sb.append((sig = Array.newInstance(c, 0).toString())
                    .substring(1, sig.indexOf('@')));
        return sb.append(')')
                .append(
                        method.getReturnType() == void.class ? "V" :
                                (sig = Array.newInstance(method.getReturnType(), 0).toString()).substring(1, sig.indexOf('@'))
                )
                .toString().replace('.', '/');
    }


    public static Set<String> findPatchClassesInDex(String dexFilePath, String suffix) {
        Set<String> classes = new HashSet<>();
        try {
            DexFile dex = new DexFile(dexFilePath);
            Enumeration<String> entries = dex.entries();
            while (entries.hasMoreElements()) {
                String entry = entries.nextElement();
                if (entry.endsWith(suffix)) {
                    classes.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }
}

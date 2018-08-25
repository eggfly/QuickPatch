package quickpatch.sdk;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class TypeUtils {
    /**
     * 使用Array.newInstance间接获取函数参数，只在函数名相同的情况下调用，性能待测试
     * TODO: 可以做缓存
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
}

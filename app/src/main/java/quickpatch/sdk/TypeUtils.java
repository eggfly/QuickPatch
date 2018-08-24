package quickpatch.sdk;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class TypeUtils {
    public static String getSignature(Method m) {
        String sig;
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> c : m.getParameterTypes())
            sb.append((sig = Array.newInstance(c, 0).toString())
                    .substring(1, sig.indexOf('@')));
        return sb.append(')')
                .append(
                        m.getReturnType() == void.class ? "V" :
                                (sig = Array.newInstance(m.getReturnType(), 0).toString()).substring(1, sig.indexOf('@'))
                )
                .toString();
    }
}

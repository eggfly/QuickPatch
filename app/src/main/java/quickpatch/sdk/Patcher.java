package quickpatch.sdk;

import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;

import quickpatch.example.MainActivity;

public final class Patcher {
    public static volatile boolean sEnablePatch = false;
    private static volatile WeakReference<PatchInterface> sPatchInterfaceRef;

    public static ProxyResult proxy(Object thisObject,
                                    String className,
                                    String methodName,
                                    String methodSignature,
                                    Object... args) {
        final ProxyResult result;
        // TODO: 判断是否有patch的逻辑、性能问题、各版本机型适配
        if (sEnablePatch) {
            // TODO classloader是否可以优化?
            final PatchInterface patch;
            if (sPatchInterfaceRef == null || sPatchInterfaceRef.get() == null) {
                patch = (PatchInterface) Proxy.newProxyInstance(Patcher.class.getClassLoader(), new Class[]{PatchInterface.class}, new PatchInvocationHandler());
                sPatchInterfaceRef = new WeakReference<>(patch);
            } else {
                patch = sPatchInterfaceRef.get();
            }
            final Object returnValue = patch.invoke(thisObject, MainActivity.class, methodName, args);
            result = ProxyResult.createActiveProxyResult(returnValue);
        } else {
            result = ProxyResult.NO_PROXY;
            // do nothing
        }
        return result;
    }
}

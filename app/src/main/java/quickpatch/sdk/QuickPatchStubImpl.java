package quickpatch.sdk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 每个修复的类中的真正实例，用来代理执行打补丁后的方法
 */
public class QuickPatchStubImpl implements QuickPatchStub {
    private final Method[] mPatchedClassMethods;
    /**
     * cache methods for speedup
     */
    private final HashMap<String, HashMap<String, Method>> mMethodAndSignatureLookupCache = new HashMap<>();
    /**
     * cache patched class constructor for speedup
     */
    private Constructor mPatchedClassConstructor;

    public QuickPatchStubImpl(Class oldClass, Class patchedClass) {
        mPatchedClassMethods = patchedClass.getDeclaredMethods();
        try {
            mPatchedClassConstructor = patchedClass.getDeclaredConstructor(Object.class);
            mPatchedClassConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        makeMethodLookupCache();
    }

    private void makeMethodLookupCache() {
        for (Method method : mPatchedClassMethods) {
            final String methodName = method.getName();
            final String methodSignature = ClassUtils.getSignature(method);
            HashMap<String, Method> map = mMethodAndSignatureLookupCache.get(methodName);
            if (map == null) {
                map = new HashMap<>();
                mMethodAndSignatureLookupCache.put(methodName, map);
            }
            map.put(methodSignature, method);
        }
    }

    public MethodProxyResult proxy(Object thisObject, String methodName, String methodSignature, Object[] args) {
        // TODO: 性能问题、各版本机型适配
        if (mPatchedClassConstructor != null) {
            HashMap<String, Method> map = mMethodAndSignatureLookupCache.get(methodName);
            if (map != null) {
                Method method = map.get(methodSignature);
                if (method != null) {
                    try {
                        Object newQPatchInstance = mPatchedClassConstructor.newInstance(thisObject);
                        final Object returnValue = method.invoke(newQPatchInstance, args);
                        return MethodProxyResult.createActiveProxyResult(returnValue);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return MethodProxyResult.NO_METHOD_PROXY;
    }
}

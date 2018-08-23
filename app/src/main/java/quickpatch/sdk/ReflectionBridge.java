package quickpatch.sdk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 使用Java反射或者JNI反射的帮助类，方便热修复代码中调用方法，其中：
 * 1. 普通方式使用java反射调用
 * 2. super方法通过JNI方式调用非虚方法
 */
public class ReflectionBridge {

    static {
        System.loadLibrary("quickpatch");
    }

    public static Object callVirtualMethod(Object obj, String methodName, Class[] argTypes, Object[] args, boolean setAccessible) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, argTypes);
            if (setAccessible) {
                method.setAccessible(true);
            }
            return method.invoke(obj, args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object callNonPublicVirtualMethod(Object obj, String methodName, Class[] argTypes, Object[] args) {
        return callVirtualMethod(obj, methodName, argTypes, args, true);
    }

    public static Object callThisMethodNative(Object obj, String methodName, String methodSignature, Object[] invokeArgs) {
        return callNonVirtualMethod(obj, getClassName(obj), methodName, methodSignature, invokeArgs);
    }

    /**
     * 使用JNI反射的方法，调用super函数
     *
     * @param obj
     * @param methodName
     * @param methodSignature
     * @param invokeArgs
     * @return
     */
    public static Object callSuperMethodNative(Object obj, String methodName, String methodSignature, Object[] invokeArgs) {
        return callNonVirtualMethod(obj, getSuperClassName(obj), methodName, methodSignature, invokeArgs);
    }

    private static String getSuperClassName(Object obj) {
        return obj.getClass().getSuperclass().getCanonicalName().replace(".", "/");
    }

    private static String getClassName(Object obj) {
        return obj.getClass().getCanonicalName().replace(".", "/");
    }

    private static Object callNonVirtualMethod(Object obj, String classNameOfMethod, String methodName, String methodSignature, Object[] invokeArgs) {
        final char returnType = getMethodSignatureReturnType(methodSignature);
        return callNonVirtualMethod(obj, classNameOfMethod, methodName, methodSignature, returnType, invokeArgs);
    }

    private static char getMethodSignatureReturnType(String methodSignature) {
        final int returnTypeFirstIndex = methodSignature.indexOf(')') + 1;
        return methodSignature.charAt(returnTypeFirstIndex);
    }

    /**
     * 对指定的对象并指定特定类的非虚方法进行调用
     * 如果指定类没有找到这个方法，那么递归查找到有相应方法的兼容的父类
     *
     * @param obj
     * @param classNameOfMethod
     * @param methodName
     * @param methodSignature
     * @param invokeArgs
     * @return
     */
    private static native Object callNonVirtualMethod(Object obj,
                                                     String classNameOfMethod,
                                                     String methodName,
                                                     String methodSignature,
                                                     char returnType,
                                                     Object... invokeArgs);
}

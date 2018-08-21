package quickpatch.sdk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionBridge {
    public static Object callNonPublicVirtualMethod(Object obj, String methodName, Class[] argTypes, Object[] args) {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, argTypes);
            method.setAccessible(true);
            Object returnValue = method.invoke(obj, args);
            return returnValue;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}

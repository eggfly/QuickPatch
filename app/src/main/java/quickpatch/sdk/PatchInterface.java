package quickpatch.sdk;

public interface PatchInterface {
    // TODO: 还需要考虑不同参数的函数重载
    Object invoke(Object thisObject, Class clazz, String methodName, String methodSignature, Object[] args);
}

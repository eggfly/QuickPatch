package quickpatch.sdk;

public class NativeBridge {

    /**
     * 对指定对象进行特定类或者兼容的父类的非虚函数调用
     *
     * @param obj
     * @param classNameOfMethod
     * @param methodName
     * @param methodSignature
     * @param invokeArgs
     * @return
     */
    public static native Object callNonVirtualMethod(Object obj,
                                                     String classNameOfMethod,
                                                     String methodName,
                                                     String methodSignature,
                                                     char returnType,
                                                     Object... invokeArgs);

}

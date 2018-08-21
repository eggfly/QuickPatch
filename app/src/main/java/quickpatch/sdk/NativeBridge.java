package quickpatch.sdk;

public class NativeBridge {

    public static native void callNonvirtualVoidMethodTest(Object obj);

    public static native void callNonvirtualVoidMethod(Object obj,
                                                       String classNameOfMethod,
                                                       String methodName,
                                                       String methodSignature,
                                                       Object[] invokeArgs);

}

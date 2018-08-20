package quickpatch.sdk;

public class NativeBridge {

    public native void callNonvirtualVoidMethod(Object obj);

    public native void callNonvirtualVoidMethodHelper(Object obj,
                                                      String classNameOfMethod,
                                                      String methodName,
                                                      String methodSignature, Object[] invokeArgs);

}

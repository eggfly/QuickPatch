package quickpatch.sdk;

/**
 * 插入到每个类作为静态成员
 */
public interface QuickPatchStub {
    /**
     * 插桩以后每个函数调用的代理方法
     *
     * @param thisObject
     * @param methodName
     * @param methodSignature
     * @param args
     * @return
     */
    MethodProxyResult proxy(Object thisObject, String methodName, String methodSignature, Object[] args);
}

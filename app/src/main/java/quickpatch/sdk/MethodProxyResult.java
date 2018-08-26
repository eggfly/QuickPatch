package quickpatch.sdk;

public final class MethodProxyResult {
    /**
     * 享元模式,表示没有成功代理到方法时候的大部分的情况
     */
    public final static MethodProxyResult NO_METHOD_PROXY = new MethodProxyResult(false, null);
    public final boolean isPatched;
    public final Object returnValue;

    private MethodProxyResult(boolean isPatched, Object returnValue) {
        this.isPatched = isPatched;
        this.returnValue = returnValue;
    }

    /**
     * 函数proxy成功时结果的创建函数
     *
     * @param returnValue
     * @return
     */
    public static MethodProxyResult createActiveProxyResult(Object returnValue) {
        return new MethodProxyResult(true, returnValue);
    }
}

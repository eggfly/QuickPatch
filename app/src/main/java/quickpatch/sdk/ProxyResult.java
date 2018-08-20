package quickpatch.sdk;

public class ProxyResult {
    /**
     * 享元模式,表示没有代理时候的大部分的情况
     */
    public final static ProxyResult NO_PROXY = new ProxyResult(false, null);
    public final boolean isPatched;
    public final Object returnValue;

    private ProxyResult(boolean isPatched, Object returnValue) {
        this.isPatched = isPatched;
        this.returnValue = returnValue;
    }

    public static ProxyResult createActiveProxyResult(Object returnValue) {
        return new ProxyResult(true, returnValue);
    }
}

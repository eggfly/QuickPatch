package quickpatch.sdk;

/**
 * QPatch的抽象基类, 用来构造和存储thisObject对象，范型方便检查类型
 *
 * @param <T>
 */
public abstract class QPatchBase<T> {
    protected final T thisObject;

    protected QPatchBase(T thisObject) {
        this.thisObject = thisObject;
    }
}
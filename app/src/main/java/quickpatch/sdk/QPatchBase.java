package quickpatch.sdk;

/**
 * QPatch的抽象基类, 用来构造和存储thisObject对象，范型方便检查类型
 *
 * @param <T>
 */
public abstract class QPatchBase<T> {
    /**
     * 被修复的原来的类的this实例
     */
    protected final T thisObject;

    protected QPatchBase(T thisObject) {
        this.thisObject = thisObject;
    }
}

package quickpatch.sdk;

import android.util.Log;

import java.lang.reflect.Field;

/**
 * QPatch的抽象基类, 用来构造和存储thisObject对象
 */
public abstract class QPatchBase {
    /**
     * 被修复的原来的类的this实例
     */
    protected final Object thisObject;

    protected QPatchBase(Object thisObject) {
        this.thisObject = thisObject;
    }

    private Field getObjectArrayMemberStubField() {
        try {
            Field field = thisObject.getClass().getDeclaredField(SdkConstants.QPATCH_OBJECT_ARRAY_STUB_FIELD_NAME);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void initializeObjectArrayMemberStub(int size) {
        Field field = getObjectArrayMemberStubField();
        if (field != null) {
            try {
                field.set(thisObject, new Object[size]);
                Log.i(getClass().getSimpleName(), "initializeObjectArrayMemberStub with size:" + size);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected Object[] getObjectArrayMemberStub() {
        Field field = getObjectArrayMemberStubField();
        try {
            return (Object[]) field.get(thisObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}

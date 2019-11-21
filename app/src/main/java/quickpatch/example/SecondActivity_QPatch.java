package quickpatch.example;

import java.math.BigInteger;

import quickpatch.sdk.QPatchBase;

/**
 * 对应SecondActivity类的补丁类
 */
@SuppressWarnings("unused")
public class SecondActivity_QPatch extends QPatchBase {

    private static final String TAG = SecondActivity_QPatch.class.getSimpleName();

    public SecondActivity_QPatch(Object thisObject) {
        super(thisObject);
    }

    /**
     * 构造函数的补丁函数
     */
    public void __init__() {
        initializeObjectArrayMemberStub(2);
        Object[] objArray = getObjectArrayMemberStub();
        objArray[0] = "obj 0: simple string";
        objArray[1] = new BigInteger("9876543210123456789876543210123456789");
    }

    private static String staticGetText(boolean booleanValue) {
        return "here's proxy call";
    }
}

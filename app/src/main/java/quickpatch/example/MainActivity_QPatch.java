package quickpatch.example;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import quickpatch.sdk.QPatchBase;
import quickpatch.sdk.ReflectionBridge;

/**
 * 对应MainActivity类的补丁类
 * 补丁函数全部通过反射调用进来
 */
@SuppressWarnings("unused")
public class MainActivity_QPatch extends QPatchBase {

    private static final String TAG = MainActivity_QPatch.class.getSimpleName();

    public MainActivity_QPatch(Object thisObject) {
        super(thisObject);
    }

    /**
     * 构造函数的补丁函数
     */
    public void __init__() {
        // 注意!!
        // java编译器会给类的成员变量在声明时的赋值自动编译到构造函数中
        // 所以热修复构造函数的时候，需要额外给原来类中的成员重新手动写初始化的代码，否则成员变量全部是null有崩溃的危险
        // 比如下面的string就是null
        MainActivity activity = (MainActivity) thisObject;
        Log.d(TAG, "patched constructor called, thisObject.fooStringValue=" + activity.fooStringValue);
        activity.fooStringValue = "foo string value with no error";
        Log.d(TAG, "patched constructor called, thisObject.fooStringValue=" + activity.fooStringValue);
        initializeObjectArrayMemberStub(2);
        Object[] objArray = getObjectArrayMemberStub();
        objArray[0] = "obj 0: simple string";
        objArray[1] = new BigInteger("9876543210123456789876543210123456789");
    }

    /**
     * 补丁函数
     * super函数或非public函数需要使用ReflectionBridge反射调用
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity) thisObject;
        ReflectionBridge.callSuperMethodNative(thisObject, "onCreate",
                "(Landroid/os/Bundle;)V", new Object[]{savedInstanceState});
        Log.d(TAG, "onCreate patched method called, thisObject.fooStringValue=" + activity.fooStringValue);
        Object[] objArray = getObjectArrayMemberStub();
        Log.d(TAG, "objArray[0]=" + objArray[0]);
        Log.d(TAG, "objArray[1]=" + objArray[1]);
        activity.setContentView(R.layout.activity_main);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(actionBar.getTitle() + " (pid=" + Process.myPid() + ")");
        TextView tv = activity.findViewById(R.id.text);
        tv.setText("Hello bug-free world!");
        tv.setTextColor(activity.getResources().getColor(android.R.color.holo_green_dark));
        Object returnValue1 = ReflectionBridge.callThisMethodNative(thisObject, "testProtectedIntMethod", "()I", new Object[0]);
        Log.d(TAG, "returnValue1: " + returnValue1);
        boolean isFinishing = activity.isFinishing();
        Log.d(TAG, "isFinishing: " + isFinishing);
        Object returnValue2 = ReflectionBridge.callNonPublicVirtualMethod(thisObject, "testProtectedIntArrayMethod", new Class[]{boolean.class}, new Object[]{false});
        Log.d(TAG, "returnValue2: " + returnValue2);
        LottieAnimationView lottieView = activity.findViewById(R.id.lottie);
        lottieView.setAnimation("oh_yes.json");
        activity.findViewById(R.id.benchmark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.benchmark();
            }
        });
        activity.findViewById(R.id.open_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, SecondActivity.class));
            }
        });
    }

    public boolean isFinishing() {
        return (boolean) ReflectionBridge.callSuperMethodNative(thisObject, "isFinishing", "()Z", new Object[0]);
    }

    // add new instance method
    public ArrayList<Object>[][] test(HashMap<Object, ArrayList<Boolean>>[][] arg) {
        return null;
    }

    // add new static method
    public static ArrayList<Object>[][] testStatic(HashMap<Object, ArrayList<Boolean>>[][] arg) {
        return null;
    }
}

package quickpatch.example;

import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.TextView;

import quickpatch.sdk.ReflectionBridge;

/**
 * 对应MainActivity类的补丁类!!
 * 补丁函数全部写成static, 并通过反射调用进来
 * TODO: 构造函数热修复
 * TODO: 增加成员变量
 */
@SuppressWarnings("unused")
public class MainActivity_QPatch {
    private static final String TAG = MainActivity_QPatch.class.getSimpleName();
    // TODO: change thisObject to MainActivity

    /**
     * TODO: 补丁函数第一个参数是原来的this对象，或者统一弄一个成员变量?
     * super函数或非public函数需要使用ReflectionBridge反射调用
     *
     * @param thisObject
     * @param savedInstanceState
     */
    public static void onCreate(Object thisObject, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) thisObject;
        ReflectionBridge.callSuperMethodNative(thisObject, "onCreate",
                "(Landroid/os/Bundle;)V", new Object[]{savedInstanceState});
        Log.d(TAG, "onCreate");
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
    }
}

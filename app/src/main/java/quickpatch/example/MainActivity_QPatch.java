package quickpatch.example;

import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.HashMap;

import quickpatch.sdk.QPatchBase;
import quickpatch.sdk.ReflectionBridge;

/**
 * 对应MainActivity类的补丁类
 * 补丁函数全部写成static, 并通过反射调用进来
 * TODO: 构造函数热修复
 * TODO: 增加成员变量
 */
@SuppressWarnings("unused")
public class MainActivity_QPatch extends QPatchBase<MainActivity> {

    private static final String TAG = MainActivity_QPatch.class.getSimpleName();

    public MainActivity_QPatch(MainActivity thisObject) {
        super(thisObject);
    }

    /**
     * 补丁函数
     * super函数或非public函数需要使用ReflectionBridge反射调用
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        ReflectionBridge.callSuperMethodNative(thisObject, "onCreate",
                "(Landroid/os/Bundle;)V", new Object[]{savedInstanceState});
        Log.d(TAG, "onCreate");
        thisObject.setContentView(R.layout.activity_main);
        ActionBar actionBar = thisObject.getSupportActionBar();
        actionBar.setTitle(actionBar.getTitle() + " (pid=" + Process.myPid() + ")");
        TextView tv = thisObject.findViewById(R.id.text);
        tv.setText("Hello bug-free world!");
        tv.setTextColor(thisObject.getResources().getColor(android.R.color.holo_green_dark));
        Object returnValue1 = ReflectionBridge.callThisMethodNative(thisObject, "testProtectedIntMethod", "()I", new Object[0]);
        Log.d(TAG, "returnValue1: " + returnValue1);
        boolean isFinishing = thisObject.isFinishing();
        Log.d(TAG, "isFinishing: " + isFinishing);
        Object returnValue2 = ReflectionBridge.callNonPublicVirtualMethod(thisObject, "testProtectedIntArrayMethod", new Class[]{boolean.class}, new Object[]{false});
        Log.d(TAG, "returnValue2: " + returnValue2);
        LottieAnimationView lottieView = thisObject.findViewById(R.id.lottie);
        lottieView.setAnimation("oh_yes.json");
        thisObject.findViewById(R.id.benchmark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisObject.benchmark();
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

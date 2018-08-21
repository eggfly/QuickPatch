package quickpatch.sdk;

import android.util.Log;
import android.view.View;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import quickpatch.example.MainActivity;
import quickpatch.example.R;

public class PatchInvocationHandler implements InvocationHandler {
    private static final String TAG = PatchInvocationHandler.class.getSimpleName();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object thisObject = args[0];
        Class clazz = (Class) args[1];
        final boolean isStaticMethod = thisObject == null;
        String methodName = (String) args[2];
        Object[] invokeArgs = (Object[]) args[3];
        // TODO: 还需要考虑不同参数的函数重载
        // Log.d(TAG, "invoke() called, class: " + clazz.getCanonicalName() + ", method: " + methodName + ", isStatic: " + isStaticMethod);
        if ("onCreate".equals(methodName)) {
            final MainActivity activity = (MainActivity) thisObject;
            NativeBridge.callNonvirtualVoidMethod(activity,
                    activity.getClass().getSuperclass().getCanonicalName().replace(".", "/"),
                    "onCreate",
                    "(Landroid/os/Bundle;)V", invokeArgs);
            activity.setContentView(R.layout.activity_main);
            activity.findViewById(R.id.enable_proxy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.sEnablePatch = true;
                }
            });
            activity.findViewById(R.id.disable_proxy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.sEnablePatch = false;
                }
            });
            Log.d(TAG, "isFinishing:" + activity.isFinishing());
            Object returnValue = ReflectionBridge.callNonPublicVirtualMethod(activity, "testProtectedIntMethod",
                    new Class[]{}, new Object[]{});
            Log.d(TAG, "callNonPublicVirtualMethod: testProtectedIntMethod returned:" + returnValue);
            return null;
        } else if ("staticGetText".equals(methodName)) {
            return (byte) 'X'; // TODO type check
        } else if ("toString".equals(methodName)) {
            return "toString...";
        } else {
            return null;
        }
    }
}

package quickpatch.sdk;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
            NativeBridge.callNonVirtualMethod(activity,
                    activity.getClass().getSuperclass().getCanonicalName().replace(".", "/"),
                    "onCreate",
                    "(Landroid/os/Bundle;)V", 'V', invokeArgs);
            activity.setContentView(R.layout.activity_main);
            activity.findViewById(R.id.enable_patch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.sEnablePatch = true;
                }
            });
            activity.findViewById(R.id.enable_patch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.sEnablePatch = false;
                }
            });
            TextView tv = activity.findViewById(R.id.text);
            tv.setText("Hello bug-free world!");
            Log.d(TAG, "isFinishing:" + activity.isFinishing());
            Object returnValue1 = ReflectionBridge.callNonPublicVirtualMethod(activity, "testProtectedIntMethod",
                    new Class[]{}, new Object[]{});
            Log.d(TAG, "callNonPublicVirtualMethod: testProtectedIntMethod returned: " + returnValue1);
            Object returnValue2 = ReflectionBridge.callNonPublicVirtualMethod(activity, "testProtectedIntArrayMethod",
                    new Class[]{}, new Object[]{});
            Log.d(TAG, "callNonPublicVirtualMethod: testProtectedIntArrayMethod returned: " + returnValue2);
            Object shouldVoid = NativeBridge.callNonVirtualMethod(thisObject, thisObject.getClass().getCanonicalName().replace(".", "/"),
                    "testPrivateVoidMethod", "()V", 'V');
            Log.d(TAG, "callNonVirtualMethod: testPrivateVoidMethod returned: " + shouldVoid);
            return null;
        } else if ("staticGetText".equals(methodName)) {
            return (byte) 'X'; // TODO type check
        } else if ("toString".equals(methodName)) {
            return "toString...";
        } else if ("isFinishing".equals(methodName)) {
            Object returnValue = NativeBridge.callNonVirtualMethod(thisObject,
                    thisObject.getClass().getSuperclass().getCanonicalName().replace(".", "/"), "isFinishing", "()Z", 'Z', invokeArgs);
            Log.d(TAG, "callNonVirtualMethod: isFinishing returned: " + returnValue);
            return returnValue;
        } else if ("testProtectedIntArrayMethod".equals(methodName)) {
            return new int[]{4, 5, 6};
        } else {
            return null;
        }
    }
}

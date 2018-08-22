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
        String methodSignature = (String) args[3];
        Object[] invokeArgs = (Object[]) args[4];
        Log.d(TAG, "invoke() called, thisObject: " + thisObject + ", class: " + clazz.getCanonicalName() + ", method: " + methodName + methodSignature + ", isStatic: " + isStaticMethod);
        if ("onCreate".equals(methodName) && "(Landroid/os/Bundle;)V".equals(methodSignature)) {
            final MainActivity activity = (MainActivity) thisObject;
            ReflectionBridge.callSuperMethodNative(activity, methodName, methodSignature, invokeArgs);
            activity.setContentView(R.layout.activity_main);
            activity.findViewById(R.id.enable_patch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.getInstance().testLoadPatch(activity);
                }
            });
            activity.findViewById(R.id.enable_patch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
            Object shouldVoid = ReflectionBridge.callThisMethodNative(thisObject,
                    "testPrivateVoidMethod", "()V", new Object[]{});
            Log.d(TAG, "callNonVirtualMethod: testPrivateVoidMethod returned: " + shouldVoid);
            return null;
        } else if ("staticGetText".equals(methodName)) {
            return (byte) 'X';
        } else if ("toString".equals(methodName)) {
            return "toString...";
        } else if ("isFinishing".equals(methodName)) {
            Object returnValue = ReflectionBridge.callSuperMethodNative(thisObject, "isFinishing", "()Z", invokeArgs);
            Log.d(TAG, "callNonVirtualMethod: isFinishing returned: " + returnValue);
            return returnValue;
        } else if ("testProtectedIntArrayMethod".equals(methodName)) {
            return new int[]{4, 5, 6};
        } else {
            return null;
        }
    }
}

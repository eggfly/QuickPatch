package quickpatch.example;

import android.util.Log;
import android.view.View;

import quickpatch.sdk.QPatchBase;

/**
 * 对应MainActivity$1类的补丁类，现在暂时走不到
 */
@SuppressWarnings("unused")
public class MainActivity$1_QPatch extends QPatchBase {

    private static final String TAG = MainActivity$1_QPatch.class.getSimpleName();

    public MainActivity$1_QPatch(Object thisObject) {
        super(thisObject);
    }

    public void onClick(View view) {
        Log.d(TAG, "patched onClicked called, thisObject=" + thisObject);
        // TODO 囧了，怎么拿到MainActivity对象?
    }
}

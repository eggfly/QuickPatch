package quickpatch.example;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import quickpatch.sdk.ReflectionBridge;

@SuppressWarnings("unused")
public class MainActivityPatch {
    // TODO: change thisObject to MainActivity
    public static void onCreate(Object thisObject, Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) thisObject;
        ReflectionBridge.callSuperMethodNative(thisObject, "onCreate",
                "(Landroid/os/Bundle;)V", new Object[]{savedInstanceState});
        Log.d("BLALA", "onCreate");
        activity.setContentView(R.layout.activity_main);
        TextView tv = activity.findViewById(R.id.text);
        tv.setText("Hello bug-free world!");
    }
}

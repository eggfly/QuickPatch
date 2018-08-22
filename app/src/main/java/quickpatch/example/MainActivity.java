package quickpatch.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import quickpatch.sdk.Patcher;
import quickpatch.sdk.ProxyResult;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    static {
        System.loadLibrary("quickpatch");
    }

    public MainActivity() {
        // TODO: hot fix <init>
        Log.d(TAG, "MainActivity.<init>()");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // super.onCreate(savedInstanceState);

        // System.out.println("testing call non-virtual method:");

        // test helper
//        nativeBridge.callNonvirtualVoidMethod(new SubSubClass(),
//                "quickpatch/example/SubClass",
//                "foo",
//                "()V");

        final ProxyResult proxyResult = Patcher.proxy(this,
                "quickpatch.example.MainActivity",
                "onCreate", "(Landroid/os/Bundle;)V",
                new Object[]{savedInstanceState});
        if (proxyResult.isPatched) {
            // return proxyResult.returnValue;
        } else {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            // TextView text = findViewById(R.id.text);
            // text.setText("HELLO BUG WORLD: " + staticGetText(false));
            findViewById(R.id.enable_patch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.getInstance().testLoadPatch(MainActivity.this);
                }
            });
            findViewById(R.id.disable_patch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            findViewById(R.id.open_second).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SecondActivity.class));
                }
            });
            Log.d(TAG, "isFinishing: " + isFinishing());
            testProtectedIntMethod();
        }
    }

    @Override
    public boolean isFinishing() {
        final ProxyResult proxyResult = Patcher.proxy(this,
                "quickpatch.example.MainActivity",
                "isFinishing", "()Z", new Object[]{});
        if (proxyResult.isPatched) {
            return (boolean) proxyResult.returnValue;
        }
        return super.isFinishing();
    }

    protected int testProtectedIntMethod() {
        Log.d(TAG, "testProtectedIntMethod called");
        return 888;
    }

    protected int[] testProtectedIntArrayMethod() {
        final ProxyResult proxyResult = Patcher.proxy(this,
                "quickpatch.example.MainActivity",
                "testProtectedIntArrayMethod", "()[I", new Object[]{});
        if (proxyResult.isPatched) {
            return (int[]) proxyResult.returnValue;
        }
        int[] array = new int[]{1, 2, 3};
        return array;
    }

    private void testPrivateVoidMethod() {
        Log.d(TAG, "testPrivateVoidMethod() called");
    }
}

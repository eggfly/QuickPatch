package quickpatch.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import quickpatch.sdk.NativeBridge;
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

        System.out.println("testing call non-virtual method:");
        NativeBridge.callNonvirtualVoidMethodTest(new SubSubClass());

        // test helper
//        nativeBridge.callNonvirtualVoidMethod(new SubSubClass(),
//                "quickpatch/example/SubClass",
//                "foo",
//                "()V");

        final ProxyResult proxyResult = Patcher.proxy(this,
                "quickpatch.example.MainActivity",
                "onCreate", "(Landroid/os/Bundle;)V",
                savedInstanceState);
        if (proxyResult.isPatched) {
            // return proxyResult.returnValue;
        } else {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            // TextView text = findViewById(R.id.text);
            // text.setText("HELLO BUG WORLD: " + staticGetText(false));
            findViewById(R.id.enable_proxy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.sEnablePatch = true;
                }
            });
            findViewById(R.id.disable_proxy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.sEnablePatch = false;
                }
            });
            findViewById(R.id.open_second).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SecondActivity.class));
                }
            });
            Log.d(TAG, "isFinishing:" + isFinishing());
            testProtectedIntMethod();
        }
    }

    @Override
    public boolean isFinishing() {
        final ProxyResult proxyResult = Patcher.proxy(this,
                "quickpatch.example.MainActivity",
                "isFinishing", "()Z");
        if (proxyResult.isPatched) {
            return proxyResult.isPatched;
        }
        return super.isFinishing();
    }

    protected int testProtectedIntMethod() {
        Log.d(TAG, "testProtectedIntMethod called");
        return 888;
    }
}

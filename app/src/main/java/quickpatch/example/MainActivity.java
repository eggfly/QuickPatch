package quickpatch.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import quickpatch.sdk.NativeBridge;
import quickpatch.sdk.Patcher;
import quickpatch.sdk.ProxyResult;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("quickpatch");
    }

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NativeBridge nativeBridge = new NativeBridge();
        System.out.println("testing call non-virtual method:");
        nativeBridge.callNonvirtualVoidMethod(new SubSubClass());
        // test helper
        nativeBridge.callNonvirtualVoidMethodHelper(new SubSubClass(),
                "quickpatch/example/SubClass",
                "foo",
                "()V");

        final ProxyResult proxyResult = Patcher.proxy(this,
                "quickpatch.example.MainActivity",
                "onCreate", "(Landroid/os/Bundle;)V",
                savedInstanceState);
        if (proxyResult.isPatched) {
            // return proxyResult.returnValue;
        } else {
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
        }
    }

}

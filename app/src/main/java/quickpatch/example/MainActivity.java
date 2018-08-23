package quickpatch.example;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import quickpatch.sdk.Patcher;
import quickpatch.sdk.ProxyResult;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public MainActivity() {
        // TODO: hot fix <init>
        Log.d(TAG, "MainActivity.<init>()");
    }

    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkStoragePermission();
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
            findViewById(R.id.enable_patch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.getInstance().testLoadPatch(MainActivity.this);
                    finish();
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    Toast.makeText(MainActivity.this, "补丁已加载", Toast.LENGTH_SHORT).show();
                }
            });
            findViewById(R.id.disable_patch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Patcher.getInstance().unloadPatch(MainActivity.this);
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

    private void checkStoragePermission() {
        int state = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        if (state != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void testPrivateVoidMethod() {
        Log.d(TAG, "testPrivateVoidMethod() called");
    }
}

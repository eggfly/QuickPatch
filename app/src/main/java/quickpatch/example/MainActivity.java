package quickpatch.example;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import quickpatch.sdk.Patcher;
import quickpatch.sdk.MethodProxyResult;
import quickpatch.sdk.QuickPatchStub;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    public static QuickPatchStub _QPatchStub;
    public Object[] _QFieldStub;
    private static final String TAG = MainActivity.class.getSimpleName();
    public String fooStringValue = "foo string value with error";

    public MainActivity() {
        // TODO: hot fix <init>
        Log.d(TAG, "MainActivity.<init>() called");
    }

    public MainActivity(boolean notUsedConstructorTest) {
        if (_QPatchStub != null) {
            final MethodProxyResult methodProxyResult = _QPatchStub.proxy(this,

                    "<init>", "(Z)V",
                    new Object[]{notUsedConstructorTest});
            if (methodProxyResult.isPatched) {
                return;
            }
        }
        Log.d(TAG, "notUsedConstructorTest");
    }

    static {
        Log.d(TAG, "MainActivity.<clinit>()");
    }

    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (_QPatchStub != null) {
            final MethodProxyResult methodProxyResult = _QPatchStub.proxy(this,
                    "onCreate", "(Landroid/os/Bundle;)V",
                    new Object[]{savedInstanceState});
            if (methodProxyResult.isPatched) {
                return;
            }
        }
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate origin method called, thisObject.fooStringValue=" + this.fooStringValue);
        checkStoragePermission();
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(actionBar.getTitle() + " (pid=" + Process.myPid() + ")");
        findViewById(R.id.enable_patch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Patcher.getInstance().loadPatch(MainActivity.this);
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(MainActivity.this, "未发现载dex补丁文件，使用apk里的_QPatch类模拟效果", Toast.LENGTH_SHORT).show();
                    Patcher.getInstance().simulateLoadPatch(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "已加载补丁:\n" + path, Toast.LENGTH_SHORT).show();
                }
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
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
        findViewById(R.id.benchmark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                benchmark();
            }
        });
        Log.d(TAG, "isFinishing: " + isFinishing());
        testProtectedIntMethod();
    }

    void benchmark() {
        final int loops = 10000;
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            isFinishing();
        }
        final long time = System.currentTimeMillis() - startTime;
        Toast.makeText(MainActivity.this, loops + " loops cost " + time + "ms", Toast.LENGTH_LONG).show();
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
        if (_QPatchStub != null) {
            final MethodProxyResult methodProxyResult = _QPatchStub.proxy(this,
                    "isFinishing", "()Z", new Object[]{});
            if (methodProxyResult.isPatched) {
                return (boolean) methodProxyResult.returnValue;
            }
        }
        return super.isFinishing();
    }

    protected int testProtectedIntMethod() {
        Log.d(TAG, "testProtectedIntMethod called");
        return 888;
    }

    protected int[] testProtectedIntArrayMethod(boolean bool) {
        if (_QPatchStub != null) {
            final MethodProxyResult methodProxyResult = _QPatchStub.proxy(this,
                    "testProtectedIntArrayMethod", "()[I", new Object[]{});
            if (methodProxyResult.isPatched) {
                return (int[]) methodProxyResult.returnValue;
            }
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

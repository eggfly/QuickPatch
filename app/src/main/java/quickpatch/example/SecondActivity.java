package quickpatch.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import quickpatch.sdk.MethodProxyResult;
import quickpatch.sdk.QuickPatchStub;

public class SecondActivity extends AppCompatActivity {

    public static QuickPatchStub _QPatchStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (_QPatchStub != null) {
            final MethodProxyResult methodProxyResult = _QPatchStub.proxy(this, "onCreate",
                    "(Landroid/os/Bundle;)V", new Object[]{savedInstanceState});
            if (methodProxyResult.isPatched) {
                return;
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        TextView tv = findViewById(R.id.text);
        tv.setText(staticGetText(true));
    }

    private static String staticGetText(boolean booleanValue) {
        return "here's nothing right now";
    }
}

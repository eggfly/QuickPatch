package quickpatch.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import quickpatch.sdk.Patcher;
import quickpatch.sdk.ProxyResult;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ProxyResult proxyResult = Patcher.proxy(this, SecondActivity.class.getCanonicalName(), "onCreate",
                "(Landroid/os/Bundle;)V", new Object[]{savedInstanceState});
        if (proxyResult.isPatched) {
            return;
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

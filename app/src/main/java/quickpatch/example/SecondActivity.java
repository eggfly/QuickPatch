package quickpatch.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        TextView tv = findViewById(R.id.text);
        tv.setText(staticGetText(true));
    }

    private static String staticGetText(boolean booleanValue) {
        return "here's nothing right now";
    }
}

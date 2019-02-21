package com.wcy.client12306;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.wcy.client12306.http.HttpUtil;
import com.wcy.client12306.util.MessageUtil;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // 继承的是AppCompatActivity时
        }
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        HttpUtil networkUtil = (HttpUtil) intent.getSerializableExtra("httpUtil");
        MessageUtil messageUtil = intent.getParcelableExtra("messageUtil");
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        tv.setText(String.format("%s\r\n\r\n%s", tv.getText(), messageUtil.getMessStr()));
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

package com.wcy.client12306.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wcy.client12306.R;
import com.wcy.client12306.service.RocketService;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    private Button mStartRocketButton;
    private Button mStopRocketButton;
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
        tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        initView();
    }

    private void initView() {
        mStartRocketButton = (Button) findViewById(R.id.start_rocket);
        mStopRocketButton = (Button) findViewById(R.id.stop_rocket);
        mStartRocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, RocketService.class));
            }
        });
        mStopRocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, RocketService.class));
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

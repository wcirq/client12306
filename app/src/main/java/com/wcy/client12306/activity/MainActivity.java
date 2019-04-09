package com.wcy.client12306.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;
import com.wcy.client12306.inter.OnLocationListener;
import com.wcy.client12306.service.RocketService;

public class MainActivity extends AppCompatActivity {
    private TextView tv;
    RocketService rocketService;
    ServiceConnection serviceConnection;
    Intent intent;
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
        if (Build.VERSION.SDK_INT >= 23) {
            if (! Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent,10);
            }
        }
        tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        initView();
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                RocketService.RocketBinder binder = (RocketService.RocketBinder) service;
                rocketService = binder.getService();
                rocketService.setOnLocationListener(new OnLocationListener() {
                    @Override
                    public void onLocation(WindowManager.LayoutParams mParams) {
                        tv.setText(String.format("%d, %d", mParams.x, mParams.y));
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        intent = new Intent(MainActivity.this, RocketService.class);
        bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
    }

    private void initView() {
        mStartRocketButton = (Button) findViewById(R.id.start_rocket);
        mStopRocketButton = (Button) findViewById(R.id.stop_rocket);
        mStartRocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
                startService(intent);
            }
        });
        mStopRocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    Toast.makeText(MainActivity.this,"没有授予权限",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this,"授权成功",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

package com.wcy.client12306.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NavigationRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;

import com.wcy.client12306.R;
import com.wcy.client12306.util.SystemUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SettingActivity extends AppCompatActivity {
    String fileName = "wallpaper.txt";
    AutoCompleteTextView autoCompleteTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide(); // 继承的是AppCompatActivity时
        }
        setContentView(R.layout.activity_setting);
        autoCompleteTextView = findViewById(R.id.wallpaper_type);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String wallpaper = SystemUtil.getFile(SettingActivity.this, fileName);
                if (wallpaper!=null){
                    Looper.prepare();
                    autoCompleteTextView.setText(wallpaper);
                    Looper.loop();
                }else {
                    SystemUtil.saveFile(SettingActivity.this,"手机壁纸", fileName);
                    Looper.prepare();
                    autoCompleteTextView.setText("手机壁纸");
                    Looper.loop();
                }
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final String type = autoCompleteTextView.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemUtil.saveFile(SettingActivity.this, type, fileName);
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}

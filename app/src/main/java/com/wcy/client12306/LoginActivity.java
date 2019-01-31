package com.wcy.client12306;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.wcy.client12306.ui.PaintView;

import java.io.File;

public class LoginActivity extends AppCompatActivity {
    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initMenu();
    }
    private void initView() {
        paintView = (PaintView) findViewById(R.id.activity_paint_pv);
    }

    private void initMenu() {
        //撤销
        menuItemSelected(R.id.activity_paint_undo, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
//                paintView.undo();
            }
        });
        //恢复
        menuItemSelected(R.id.activity_paint_redo, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
//                paintView.redo();
            }
        });

        //颜色
        menuItemSelected(R.id.activity_paint_color, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
//                paintView.setPaintColor(Color.RED);
            }
        });
        //清空
        menuItemSelected(R.id.activity_paint_clear, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
//                paintView.clearAll();
            }
        });

        //橡皮擦
        menuItemSelected(R.id.activity_paint_eraser, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
//                paintView.setEraserModel(true);
            }
        });

        //保存
        menuItemSelected(R.id.activity_paint_save, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
                String path = Environment.getExternalStorageDirectory().getPath()
                        + File.separator + "image";
                String imgName = "paint.jpg";
//                if (paintView.saveImg(path, imgName)) {
//                    ToastUtils.show(PaintViewActivity.this, "保存成功");
//                }
            }
        });
    }

    /**
     * 选中底部 Menu 菜单项
     */
    private void menuItemSelected(int viewId, final MenuSelectedListener listener) {
        findViewById(viewId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMenuSelected();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    interface MenuSelectedListener {
        void onMenuSelected();
    }

}

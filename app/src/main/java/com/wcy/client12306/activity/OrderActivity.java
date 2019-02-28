package com.wcy.client12306.activity;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;

import java.util.ArrayList;
import java.util.List;

import cn.wcy.treelibrary.Node;
import cn.wcy.treelibrary.OnExpandableItemClickListerner;
import cn.wcy.treelibrary.OnExpandableItemLongClickListener;
import cn.wcy.treelibrary.OnInnerItemClickListener;
import cn.wcy.treelibrary.OnInnerItemLongClickListener;
import cn.wcy.treelibrary.TreeAdapter;

public class OrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_order);
        ListView lv = new ListView(this);
        setContentView(lv, new ViewGroup.LayoutParams(-1, -1));
        final List<Item> list = new ArrayList<>();
        list.add(new Item(0, 0, 0, false, "Android"));
        list.add(new Item(1, 0, 1, false, "Service"));
        list.add(new Item(2, 0, 1, false, "Activity"));
        list.add(new Item(3, 0, 1, false, "Receiver"));
        list.add(new Item(4, 0, 0, true, "Java Web"));
        list.add(new Item(5, 4, 1, false, "CSS"));
        list.add(new Item(6, 4, 1, false, "Jsp"));
        list.add(new Item(7, 4, 1, true, "Html"));
        list.add(new Item(8, 7, 2, false, "p"));
        final MyAdapter adapter = new MyAdapter(lv, list);
        adapter.setOnInnerItemClickListener(new OnInnerItemClickListener<Item>() {
            @Override
            public void onClick(Item node, AdapterView<?> parent, View view, int position) {
                Toast.makeText(OrderActivity.this, "click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnInnerItemLongClickListener(new OnInnerItemLongClickListener<Item>() {
            @Override
            public void onLongClick(Item node, AdapterView<?> parent, View view, int position) {
                Toast.makeText(OrderActivity.this, "long click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnExpandableItemClickListerner(new OnExpandableItemClickListerner<Item>() {
            @Override
            public void onExpandableItemClick(Item node, AdapterView<?> parent, View view, int position) {
                Toast.makeText(OrderActivity.this, "click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnExpandableItemLongClickListener(new OnExpandableItemLongClickListener<Item>() {
            @Override
            public void onExpandableItemLongClick(Item node, AdapterView<?> parent, View view, int position) {
                Toast.makeText(OrderActivity.this, "long click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });

        lv.setAdapter(adapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                list.add(new Item(9, 7, 2, false, "a"));
                adapter.notifyDataSetChanged();
            }
        }, 2000);
    }

    private class MyAdapter extends TreeAdapter<Item> {
        MyAdapter(ListView lv, List<Item> nodes) {
            super(lv, nodes);
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        /**
         * 获取当前位置的条目类型
         */
        @Override
        public int getItemViewType(int position) {
            if (getItem(position).hasChild()) {
                return 1;
            }
            return 0;
        }

        @Override
        protected Holder<Item> getHolder(int position) {
            switch(getItemViewType(position)) {
                case 1:
                    return new Holder<Item>() {
                        private ImageView iv;
                        private TextView tv;

                        @Override
                        protected void setData(Item node, int position) {
                            iv.setVisibility(node.hasChild() ? View.VISIBLE : View.INVISIBLE);
                            iv.setBackgroundResource(node.isExpand ? R.drawable.expand : R.drawable.fold);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv.getLayoutParams();
                            params.leftMargin = (node.level + 1) * dip2px(20);
                            iv.setLayoutParams(params);
                            tv.setText(node.name);
                        }

                        @Override
                        protected View createConvertView() {
                            View view = View.inflate(OrderActivity.this, R.layout.item_tree_list_has_child, null);
                            iv = view.findViewById(R.id.ivIcon);
                            tv = view.findViewById(R.id.tvName);
                            return view;
                        }
                    };
                default:
                    return new Holder<Item>() {
                        private TextView tv;

                        @Override
                        protected void setData(Item node, int position) {
                            tv.setText(node.name);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                            params.leftMargin = (node.level + 3) * dip2px(20);
                            tv.setLayoutParams(params);
                        }

                        @Override
                        protected View createConvertView() {
                            View view = View.inflate(OrderActivity.this, R.layout.item_tree_list_no_child, null);
                            tv = view.findViewById(R.id.tvName);
                            return view;
                        }
                    };
            }
        }
    }

    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private class Item extends Node<Item> {
        String name;

        Item(int id, int pId, int level, boolean isExpand, String name) {
            super(id, pId, level, isExpand);
            this.name = name;
        }
    }
}
package com.wcy.client12306.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;
import com.wcy.client12306.http.Session;
import com.wcy.client12306.util.SystemUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.wcy.treelibrary.Node;
import cn.wcy.treelibrary.OnExpandableItemClickListerner;
import cn.wcy.treelibrary.OnExpandableItemLongClickListener;
import cn.wcy.treelibrary.OnInnerItemClickListener;
import cn.wcy.treelibrary.OnInnerItemLongClickListener;
import cn.wcy.treelibrary.TreeAdapter;

public class OrderActivity extends AppCompatActivity {
    private Session session;
    private MyAdapter adapter;
    private Handler handler;
    List<Item> list;
    ListView listView;

    private class MyHandler extends Handler {
        private final WeakReference<OrderActivity> mTarget;

        MyHandler(OrderActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            OrderActivity activity = mTarget.get();
            if (msg.what == 1) {
                adapter.notifyDataSetChanged();
            } else if (msg.what == 2) {

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        listView = new ListView(this);
        ConstraintLayout constraintLayout = findViewById(R.id.order_constraint_layout);
        constraintLayout.addView(listView);
        SystemUtil.setStatusBarColor(this, R.color.lineColor_click);
//        setContentView(listView, new ViewGroup.LayoutParams(-1, -1));
        handler = new MyHandler(OrderActivity.this);
        Intent intent = getIntent();
        session = (Session) intent.getSerializableExtra("session");
        list = new ArrayList<>();
        adapter = new MyAdapter(listView, list);
        adapter.setOnInnerItemClickListener(new OnInnerItemClickListener<Item>() {
            @Override
            public void onClick(Item node, AdapterView<?> parent, View view, int position) {
//                Toast.makeText(OrderActivity.this, "click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnInnerItemLongClickListener(new OnInnerItemLongClickListener<Item>() {
            @Override
            public void onLongClick(Item node, AdapterView<?> parent, View view, int position) {
//                Toast.makeText(OrderActivity.this, "long click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnExpandableItemClickListerner(new OnExpandableItemClickListerner<Item>() {
            @Override
            public void onExpandableItemClick(Item node, AdapterView<?> parent, View view, int position) {
//                Toast.makeText(OrderActivity.this, "click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnExpandableItemLongClickListener(new OnExpandableItemLongClickListener<Item>() {
            @Override
            public void onExpandableItemLongClick(Item node, AdapterView<?> parent, View view, int position) {
//                Toast.makeText(OrderActivity.this, "long click: " + node.name, Toast.LENGTH_SHORT).show();
            }
        });
        listView.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> paramsMap = new HashMap<>();
                paramsMap.put("come_from_flag","my_order");
                paramsMap.put("pageIndex","0");
                paramsMap.put("pageSize","8");
                paramsMap.put("query_where","H");
                paramsMap.put("queryStartDate","2018-01-01");
                long time1 = Calendar.getInstance().getTimeInMillis()-1000*3600*24;
                Date date = new Date(time1);
                paramsMap.put("queryEndDate",String.format("%d-%02d-%02d", 1900+date.getYear(), date.getMonth()+1, date.getDate()));
                paramsMap.put("queryType","1");
                paramsMap.put("sequeue_train_name","");
                JSONObject data1 = (JSONObject) session.post("https://kyfw.12306.cn/otn/login/conf", null, null);
                HashMap<String, String> paramsMap1 = new HashMap<>();
                paramsMap1.put("_json_att","");
                JSONObject data2 = (JSONObject) session.post("https://kyfw.12306.cn/otn/queryOrder/queryMyOrderNoComplete", null, paramsMap1);
                JSONObject data = (JSONObject) session.post("https://kyfw.12306.cn/otn/queryOrder/queryMyOrder", null, paramsMap);

                try {
                    int id=0;
                    JSONArray orderDTODataList = data.getJSONObject("data").getJSONArray("OrderDTODataList");
                    int tickets_length = 0;
                    for (int i=0;i<orderDTODataList.length();i++){
                        id += (i*3+tickets_length);
                        String sequence_no = orderDTODataList.getJSONObject(i).getString("sequence_no");
                        String order_date = orderDTODataList.getJSONObject(i).getString("order_date");
                        list.add(new Item(id, id, 0, false, String.format("%s    %s", order_date, sequence_no)));

                        String from_station_name_page = orderDTODataList.getJSONObject(i).getJSONArray("from_station_name_page").getString(0);
                        String to_station_name_page = orderDTODataList.getJSONObject(i).getJSONArray("to_station_name_page").getString(0);
                        String start_train_date_page = orderDTODataList.getJSONObject(i).getString("start_train_date_page");
                        String train_code_page = orderDTODataList.getJSONObject(i).getString("train_code_page");
                        list.add(new Item(id+1, id, 1, false, String.format("%s-->%s  %s\r\n  %s", from_station_name_page, to_station_name_page, train_code_page, start_train_date_page)));

                        JSONArray tickets = orderDTODataList.getJSONObject(i).getJSONArray("tickets");
                        JSONArray array_passser_name_page = orderDTODataList.getJSONObject(i).getJSONArray("array_passser_name_page");
                        tickets_length += (tickets.length()-1);
                        for (int j=0;j<tickets.length();j++){
                            String user_name = array_passser_name_page.getString(j);
                            String seat_name = tickets.getJSONObject(j).getString("seat_name");
                            String seat_type_name = tickets.getJSONObject(j).getString("seat_type_name");
                            String coach_name = tickets.getJSONObject(j).getString("coach_name");
                            String ticket_status_name = tickets.getJSONObject(j).getString("ticket_status_name");
                            String str_ticket_price_page = tickets.getJSONObject(j).getString("str_ticket_price_page");
                            list.add(new Item(id+2+j, id+1, 2, false, String.format("%s - %s ￥%s\r\n%s车%s  %s", user_name, seat_type_name, str_ticket_price_page, coach_name, seat_name, ticket_status_name)));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(1);
            }
        }).start();

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                list.add(new Item(9, 7, 2, false, "a"));
////                adapter.notifyDataSetChanged();
//            }
//        }, 5000);
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
                        RelativeLayout rlParent;
                        private ImageView iv;
                        private TextView tv;

                        @Override
                        protected void setData(Item node, int position) {
                            rlParent.setBackgroundColor(node.level==0?Color.argb(200,33, 150, 243):Color.argb(200,3, 169, 244));
                            rlParent.setPadding(0, 20, 0, 20);
                            View view = getConvertView();
//                            view.setPadding(0, 20, 0, 20);
                            iv.setVisibility(node.hasChild() ? View.VISIBLE : View.INVISIBLE);
                            iv.setBackgroundResource(node.isExpand ? R.drawable.expand : R.drawable.fold);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv.getLayoutParams();
                            params.setMargins((node.level + 1) * dip2px(20), 0, 0, 0);
//                            params.leftMargin = (node.level + 1) * dip2px(20);
                            iv.setLayoutParams(params);
                            tv.setText(node.name);
                        }

                        @Override
                        protected View createConvertView() {
                            View view = View.inflate(OrderActivity.this, R.layout.item_tree_list_has_child, null);
                            rlParent = view.findViewById(R.id.rlParent);
                            iv = view.findViewById(R.id.ivIcon);
                            tv = view.findViewById(R.id.tvName);
                            return view;
                        }
                    };
                default:
                    return new Holder<Item>() {
                        RelativeLayout rlChild;
                        private TextView tv;

                        @Override
                        protected void setData(Item node, int position) {
                            tv.setText(node.name);
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                            params.leftMargin = (node.level + 1) * dip2px(20);
                            tv.setLayoutParams(params);
                        }

                        @Override
                        protected View createConvertView() {
                            View view = View.inflate(OrderActivity.this, R.layout.item_tree_list_no_child, null);
                            rlChild = view.findViewById(R.id.rlChild);
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

    @Override
    protected void onPause() {
        super.onPause();
        Session.dump(session, null);
    }
}
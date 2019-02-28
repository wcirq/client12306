package com.wcy.client12306.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.wcy.client12306.util.OrderData;

import java.util.List;

public class OrderAdapter extends ArrayAdapter<OrderData> {

    public OrderAdapter(Context context, int resource) {
        super(context, resource);
    }

    public OrderAdapter(Context context, int resource, OrderData[] objects) {
        super(context, resource, objects);
    }

    public OrderAdapter(Context context, int resource, List<OrderData> objects) {
        super(context, resource, objects);
    }
}

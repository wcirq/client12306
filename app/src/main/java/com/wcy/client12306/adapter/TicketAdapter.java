package com.wcy.client12306.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wcy.client12306.R;
import com.wcy.client12306.util.Ticket;

import java.util.List;

public class TicketAdapter extends ArrayAdapter<Ticket> {
    int resourceID;
    public TicketAdapter(Context context, int resource, List<Ticket> objects) {
        super(context, resource, objects);
        resourceID = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Ticket ticket = getItem(position);//获得当前项fruit实例
        //动态加载布局文件
        View view = LayoutInflater.from(getContext()).inflate(resourceID,parent,false);

        TextView buyCode = view.findViewById(R.id.buy_code);
        buyCode.setText(ticket.getBuyCode());

        TextView train_id = view.findViewById(R.id.train_id);
        train_id.setText(ticket.getTrainId());

        LinearLayout linearLayout = (LinearLayout) train_id.getParent();
        if (ticket.getBuyCode().equals("")){
            linearLayout.setBackgroundColor(Color.argb(150, 150,50, 50));
        }else {
            linearLayout.setBackgroundColor(Color.argb(150, 50,150, 50));
        }

        TextView start_arrival_station = view.findViewById(R.id.start_arrival_station);
        start_arrival_station.setText(String.format("%s-->%s", ticket.getStartStation(), ticket.getArrivalStation()));

        TextView start_arrival_time = view.findViewById(R.id.start_arrival_time);
        start_arrival_time.setText(String.format("%s - %s", ticket.getStartTime(), ticket.getArrivalTime()));

        TextView through_time = view.findViewById(R.id.through_time);
        through_time.setText(ticket.getThroughTime());

        TextView specialSeat = view.findViewById(R.id.specialSeat);
        specialSeat.setText(ticket.getSpecialSeat());

        TextView levelOneSeat = view.findViewById(R.id.levelOneSeat);
        levelOneSeat.setText(ticket.getLevelOneSeat());

        TextView levelTwoSeat = view.findViewById(R.id.levelTwoSeat);
        levelTwoSeat.setText(ticket.getLevelTwoSeat());

        TextView seniorSoft = view.findViewById(R.id.seniorSoft);
        seniorSoft.setText(ticket.getSeniorSoft());

        TextView levelOneSoft = view.findViewById(R.id.levelOneSoft);
        levelOneSoft.setText(ticket.getLevelOneSoft());

        TextView bulletSoft = view.findViewById(R.id.bulletSoft);
        bulletSoft.setText(ticket.getBulletSoft());

        TextView hardsLeeper = view.findViewById(R.id.hardsLeeper);
        hardsLeeper.setText(ticket.getHardsLeeper());

        TextView softSeat = view.findViewById(R.id.softSeat);
        softSeat.setText(ticket.getSoftSeat());

        TextView hardSeat = view.findViewById(R.id.hardSeat);
        hardSeat.setText(ticket.getHardSeat());

        TextView noSeat = view.findViewById(R.id.noSeat);
        noSeat.setText(ticket.getNoSeat());

        TextView other = view.findViewById(R.id.other);
        other.setText(ticket.getOther());
        return view;
    }
}

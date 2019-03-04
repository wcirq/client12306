package com.wcy.client12306.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.wcy.client12306.R;
import com.wcy.client12306.adapter.TicketAdapter;
import com.wcy.client12306.http.Crawler;
import com.wcy.client12306.http.Session;
import com.wcy.client12306.util.Ticket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuyActivity extends AppCompatActivity {
    HashMap<String, String> stationNameCode = null;
    HashMap<String, String> stationCodeName = null;
    AutoCompleteTextView startStand;
    AutoCompleteTextView destinationStand;
    ArrayAdapter<String> startAdapter;
    ArrayAdapter<String> destinationAdapter;
    ArrayList<String> startStandList;
    ArrayList<String> destinationStandList;
    Handler handler;
    private TicketAdapter ticketAdapter;
    private Session session;
    private List<Ticket> ticketList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        startStandList = new ArrayList<>();
        destinationStandList = new ArrayList<>();
        destinationStandList.add("adad");
        destinationStandList.add("adad");
        startStand = findViewById(R.id.start_stand);
        destinationStand = findViewById(R.id.destination_stand);
        startAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                startStandList);
        destinationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                destinationStandList);
        startStand.setAdapter(startAdapter);
        destinationStand.setAdapter(destinationAdapter);
        startStand.setThreshold(1);
        startStand.setDropDownHeight(350);
        startStand.setCompletionHint("最近的5条记录");
        startStand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                startStandList.add("a");
                handler.sendEmptyMessage(2);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                startStandList.add("b");
                handler.sendEmptyMessage(2);
            }

            @Override
            public void afterTextChanged(Editable s) {
                startStandList.add("c");
                handler.sendEmptyMessage(2);
            }
        });

        handler = new MyHandler(BuyActivity.this);
        Intent intent = getIntent();
        session = (Session) intent.getSerializableExtra("session");
        ticketAdapter = new TicketAdapter(BuyActivity.this, R.layout.item_buy_list, ticketList);
        ListView listView = findViewById(R.id.ticket_list_view);
        listView.setAdapter(ticketAdapter);
        Button button = findViewById(R.id.button_query);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = "KQW";
                String destination = "KNW";
                String date = "2019-03-29";
                query(start, destination, date);
            }
        });
        initStationCode();
    }

    private void initStationCode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                stationNameCode = new HashMap<>();
                stationCodeName = new HashMap<>();
                String stationCodeStrs = (String) session.get("https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.9098",null);
                for (String stationCodeStr:stationCodeStrs.split("@")){
                    String[] stationCodeArray = stationCodeStr.split("\\|");
                    if (stationCodeArray.length>=3){
                        stationNameCode.put(stationCodeArray[1], stationCodeArray[2]);
                        stationCodeName.put(stationCodeArray[2], stationCodeArray[1]);
                    }
                }
            }
        }).start();
    }

    public void query(String start, String destination, String date){
        final String startFinal = start;
        final String destinationFinal = destination;
        final String dateFinal = date;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String pattern = "CLeftTicketUrl = 'leftTicket/(.*?)';";
                String html = (String) session.get("https://kyfw.12306.cn/otn/leftTicket/init", null);
                String query = Crawler.getMatcher(html, pattern).get(0);
                String url = "https://kyfw.12306.cn/otn/leftTicket/%s?leftTicketDTO.train_date=%s&leftTicketDTO.from_station=%s&leftTicketDTO.to_station=%s&purpose_codes=ADULT";
                url = String.format(url, query, dateFinal, startFinal, destinationFinal);
                JSONObject ticketInfo = (JSONObject) session.get(url, null);
                try {
                    JSONArray ticketsJsonArray = ticketInfo.getJSONObject("data").getJSONArray("result");
                    JSONObject stationMap = ticketInfo.getJSONObject("data").getJSONObject("map");
                    ticketList.clear();
                    for (int i=0;i<ticketsJsonArray.length();i++){
                        Ticket ticket = new Ticket();
                        String ticketArray[] = ticketsJsonArray.getString(i).split("\\|");
                        ticket.setBuyCode(ticketArray[0]);
                        ticket.setTrainId(ticketArray[3]);
                        if (stationCodeName!=null){
                            ticket.setStartStation(stationCodeName.get(ticketArray[6]));
                            ticket.setArrivalStation(stationCodeName.get(ticketArray[7]));
                        }else {
                            ticket.setStartStation(stationMap.getString(ticketArray[6]));
                            ticket.setArrivalStation(stationMap.getString(ticketArray[7]));
                        }
                        ticket.setStartTime(ticketArray[8]);
                        ticket.setArrivalTime(ticketArray[9]);
                        ticket.setThroughTime(ticketArray[10]);

                        ticket.setSpecialSeat(ticketArray[32].equals("")?"-":ticketArray[32]);
                        ticket.setLevelOneSeat(ticketArray[31].equals("")?"-":ticketArray[31]);
                        ticket.setLevelTwoSeat(ticketArray[30].equals("")?"-":ticketArray[30]);
                        ticket.setSeniorSoft(ticketArray[21].equals("")?"-":ticketArray[21]);
                        ticket.setLevelOneSoft(ticketArray[23].equals("")?"-":ticketArray[23]);
                        ticket.setBulletSoft(ticketArray[33].equals("")?"-":ticketArray[33]);
                        ticket.setHardsLeeper(ticketArray[28].equals("")?"-":ticketArray[28]);
                        ticket.setSoftSeat("-");
                        ticket.setHardSeat(ticketArray[29].equals("")?"-":ticketArray[29]);
                        ticket.setNoSeat(ticketArray[26].equals("")?"-":ticketArray[26]);
                        ticket.setOther("-");
                        ticketList.add(ticket);
                    }
                    handler.sendEmptyMessage(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class MyHandler extends Handler {
        private final WeakReference<BuyActivity> mTarget;

        MyHandler(BuyActivity target) {
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            BuyActivity activity = mTarget.get();
            if (msg.what == 1) {
                ticketAdapter.notifyDataSetChanged();
            } else if (msg.what == 2) {
                startAdapter.notifyDataSetChanged();
                destinationAdapter.notifyDataSetChanged();
                startStand.showDropDown();
            }
        }
    }
}

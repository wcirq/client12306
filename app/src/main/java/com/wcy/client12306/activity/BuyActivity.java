package com.wcy.client12306.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wcy.client12306.R;
import com.wcy.client12306.adapter.TicketAdapter;
import com.wcy.client12306.http.Crawler;
import com.wcy.client12306.http.Session;
import com.wcy.client12306.util.Ticket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class BuyActivity extends AppCompatActivity {
    HashMap<String, String> stationNameCode = null;
    HashMap<String, String> stationCodeName = null;
    AutoCompleteTextView startStand;
    AutoCompleteTextView destinationStand;
    EditText goTime;
    ArrayAdapter<String> startAdapter;
    ArrayAdapter<String> destinationAdapter;
    ArrayList<String> startStandList;
    ArrayList<String> destinationStandList;
    Handler handler;
    private TicketAdapter ticketAdapter;
    private Session session;
    private List<Ticket> ticketList = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();
    final int[] YEAR = {calendar.get(Calendar.YEAR)};
    final int year_final = YEAR[0];
    final int[] MONTH = {calendar.get(Calendar.MONTH)};
    final int month_final = MONTH[0];
    final int[] DAY = {calendar.get(Calendar.DAY_OF_MONTH)};
    final int day_final = DAY[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        startStandList = new ArrayList<>();
        destinationStandList = new ArrayList<>();
        startStand = findViewById(R.id.start_stand);
        destinationStand = findViewById(R.id.destination_stand);
        goTime = findViewById(R.id.go_time);
        goTime.setText(String.format("%d-%02d-%02d", year_final, month_final + 1, day_final));
        goTime.setOnClickListener(new View.OnClickListener() {
            public Date string2date(String date) {
                DateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    return sf.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Date dateNow = string2date(String.format("%d-%d-%d", year_final, month_final+1,day_final));
                        Date dateChoose = string2date(String.format("%d-%d-%d", year, month+1,day));
                        long days=(dateChoose.getTime()-dateNow.getTime())/(1000*3600*24);
                        Log.d("days", String.valueOf(days));
                        if (days>=30){
                            Toast.makeText(BuyActivity.this,"这一天的票还没开卖，请重新选择!", Toast.LENGTH_SHORT).show();
                            goTime.setText(String.format("%d-%02d-%02d", year_final, month_final + 1, day_final));
                            goTime.setText(String.format("%d-%02d-%02d", year_final, month_final + 1, day_final));
                            YEAR[0] = year_final;
                            MONTH[0] =month_final;
                            DAY[0] = day_final;
                        }else if (days<0){
                            Toast.makeText(BuyActivity.this,"不能买以前的票!", Toast.LENGTH_SHORT).show();
                            goTime.setText(String.format("%d-%02d-%02d", year_final, month_final + 1, day_final));
                            YEAR[0] = year_final;
                            MONTH[0] =month_final;
                            DAY[0] = day_final;
                        }else {
                            goTime.setText(String.format("%d-%02d-%02d", year, month + 1, day));
                            YEAR[0] = year;
                            MONTH[0] =month;
                            DAY[0] = day;
                        }
                    }
                };
                DatePickerDialog dialog=new DatePickerDialog(BuyActivity.this, 0,listener, YEAR[0], MONTH[0], DAY[0]);
                dialog.show();
            }
        });

        startAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_buy_hint_list,
                startStandList);
        destinationAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_buy_hint_list,
                destinationStandList);
        startStand.setAdapter(startAdapter);
        destinationStand.setAdapter(destinationAdapter);
        startStand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                handler.sendEmptyMessage(2);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                handler.sendEmptyMessage(2);
            }

            @Override
            public void afterTextChanged(Editable s) {
//                handler.sendEmptyMessage(2);
            }
        });
        startStand.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    startStand.showDropDown();
                }else {
                    startStand.dismissDropDown();
                }
            }
        });
        destinationStand.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    destinationStand.showDropDown();
                }else {
                    destinationStand.dismissDropDown();
                }
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
                String start = stationNameCode.get(startStand.getText().toString());
                String destination = stationNameCode.get(destinationStand.getText().toString());
                String date = goTime.getText().toString();
                query(start, destination, date);
            }
        });
        initStationCode();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_name = (TextView) view.findViewById(R.id.train_id);
                TextView buy_code = (TextView) view.findViewById(R.id.buy_code);
                if (buy_code.getText().equals("")){
                    Toast.makeText(BuyActivity.this, "车次:" + tv_name.getText() + "\r\n没有票，无法购买!" + buy_code.getText().toString(), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(BuyActivity.this, "车次:" + tv_name.getText() + "\r\n" + buy_code.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                Set<String> keys=stationNameCode.keySet();
                startStandList.addAll(keys);
                destinationStandList.addAll(keys);
                Log.d("","");
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
//                startStand.showDropDown();
//                startStand.dismissDropDown();
            }
        }
    }
}

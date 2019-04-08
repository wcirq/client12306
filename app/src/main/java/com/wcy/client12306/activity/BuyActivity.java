package com.wcy.client12306.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
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
    String date;
    String start;
    String destination;
    ArrayList<String[]> ticketArrays = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        startStandList = new ArrayList<>();
        destinationStandList = new ArrayList<>();
        startStand = findViewById(R.id.start_stand);
        destinationStand = findViewById(R.id.destination_stand);
        goTime = findViewById(R.id.go_time);
        goTime.setText(String.format("%d-%02d-%02d", year_final, month_final + 1, day_final + 1));
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
                start = stationNameCode.get(startStand.getText().toString());
                destination = stationNameCode.get(destinationStand.getText().toString());
                date = goTime.getText().toString();
                if (start==null){
                    Toast.makeText(BuyActivity.this, "请输入正确的出发地", Toast.LENGTH_SHORT).show();
                }else if (destination==null){
                    Toast.makeText(BuyActivity.this, "请输入正确的目的地", Toast.LENGTH_SHORT).show();
                }else {
                    query(start, destination, date);
                }
            }
        });
        initStationCode();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                final TextView tv_name = (TextView) view.findViewById(R.id.train_id);
                final TextView buy_code = (TextView) view.findViewById(R.id.buy_code);
                if (buy_code.getText().equals("")){
                    Toast.makeText(BuyActivity.this, "车次:" + tv_name.getText() + "\r\n没有票，无法购买!" + buy_code.getText().toString(), Toast.LENGTH_SHORT).show();
                }else {
                    // Toast.makeText(BuyActivity.this, "车次:" + tv_name.getText() + "\r\n" + buy_code.getText().toString(), Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String url = "https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest";
                            HashMap<String, String> agrs = new HashMap<>();
                            agrs.put("secretStr",buy_code.getText().toString());
                            agrs.put("train_date",date);
                            Calendar calendar = Calendar.getInstance();
                            agrs.put("back_train_date",String.format("%d-%02d-%02d",calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
                            agrs.put("tour_flag","dc");
                            agrs.put("purpose_codes","ADULT");
                            agrs.put("query_from_station_name",start);
                            agrs.put("query_to_station_name",destination);
                            agrs.put("undefined","");

                            JSONObject resp = (JSONObject) session.post(url, null, agrs);
                            Log.d("submitOrderRequest", resp.toString());
                            try {
                                if (resp.getBoolean("status")){
                                    if (resp.getString("data").equals("N")){
//                                        HashMap<String, String> args = new HashMap<>();
//
//
//                                        args.clear();
//                                        args.put("passengerTicketStr", "");
//                                        args.put("oldPassengerStr", "");
//                                        args.put("REPEAT_SUBMIT_TOKEN", "");
//                                        args.put("randCode", "");
//                                        args.put("cancel_flag", "2");
//                                        args.put("canbed_level_order_numcel_flag", "000000000000000000000000000000");
//                                        args.put("tour_flag", "dc");
//                                        args.put("_json_att", "");
//                                        url = "https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo";
//                                        session.post(url, null, args);

                                        url = "https://kyfw.12306.cn/otn/confirmPassenger/initDc";
                                        HashMap<String, String> data = new HashMap<>();
                                        data.put("_json_att","");
                                        String html = (String) session.post(url, null, data);
                                        String globalRepeatSubmitToken = Crawler.getMatcher(html, "globalRepeatSubmitToken = '(.*?)';").get(0);
                                        String key_check_isChange = Crawler.getMatcher(html, "'key_check_isChange':'(.*?)'").get(0);
                                        String purpose_codes = Crawler.getMatcher(html, "'purpose_codes':'(.*?)'").get(0);

                                        url = "https://kyfw.12306.cn/otn/confirmPassenger/getPassengerDTOs";
                                        data.clear();
                                        data.put("_json_att","");
                                        data.put("REPEAT_SUBMIT_TOKEN",globalRepeatSubmitToken);
                                        JSONObject res = (JSONObject) session.post(url, null, data);
                                        Log.d("getPassengerDTOs", res.toString());

                                        url = "https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo";
                                        data.clear();
                                        data.put("cancel_flag","2");
                                        data.put("bed_level_order_num","000000000000000000000000000000");
                                        /*passengerTicketStr组成的格式：seatType,0,票类型（成人票填1）,乘客名,passenger_id_type_code,passenger_id_no,mobile_no,’N’
                                        多个乘车人用’_’隔开
                                        oldPassengerStr组成的格式：乘客名,passenger_id_type_code,passenger_id_no,passenger_type，’_’
                                        多个乘车人用’_’隔开，注意最后的需要多加一个’_’。*/
                                        data.put("passengerTicketStr","O,0,1,吴臣杨,1,522631199402283114,18685134228,N");
                                        data.put("oldPassengerStr","吴臣杨,1,522631199402283114,1_");
                                        data.put("tour_flag","dc");
                                        data.put("randCode","");
                                        data.put("whatsSelect","1");
                                        data.put("_json_att","");
                                        data.put("REPEAT_SUBMIT_TOKEN",globalRepeatSubmitToken);
                                        JSONObject json = (JSONObject) session.post(url, null, data);
                                        Log.d("checkOrderInfo", json.toString());
                                        if (json.getJSONObject("data").getBoolean("submitStatus")){
                                            url = "https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount";
                                            data.clear();
                                            DateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                                            String date_parse = sf.parse(date).toString();
                                            date_parse = date_parse.substring(0,11)+date_parse.substring(30,date_parse.length())+" 00:00:00 GMT+0800 (中国标准时间)";
                                            // date_parse = date_parse.replace(" ", "+");
                                            data.put("train_date",date_parse);
                                            data.put("train_no",ticketArrays.get(position)[2]);
                                            data.put("stationTrainCode",ticketArrays.get(position)[3]);
                                            /*  ‘硬卧’ => ‘3’,
                                                ‘软卧’ => ‘4’,
                                                ‘二等座’ => ‘O’,
                                                ‘一等座’ => ‘M’,
                                                ‘硬座’ => ‘1’,*/
                                            data.put("seatType","O");
                                            data.put("fromStationTelecode",ticketArrays.get(position)[6]);
                                            data.put("toStationTelecode",ticketArrays.get(position)[7]);
                                            data.put("leftTicket",ticketArrays.get(position)[12]);
                                            data.put("purpose_codes",purpose_codes);
                                            data.put("train_location",ticketArrays.get(position)[15]);
                                            data.put("_json_att","");
                                            data.put("REPEAT_SUBMIT_TOKEN",globalRepeatSubmitToken);
                                            HashMap<String, HashMap<String, String>> cookies = new HashMap<>();
                                            HashMap<String, String> cookie = new HashMap<>();
                                            cookie.put("_jc_save_fromDate", "2019-04-01");
                                            cookie.put("_jc_save_fromStation", "北京,BJP");
                                            cookie.put("_jc_save_showIns", "true");
                                            cookie.put("_jc_save_toDate", "2019-03-06");
                                            cookie.put("_jc_save_toStation", "上海,SHH");
                                            cookie.put("_jc_save_wfdc_flag", "dc");
                                            cookies.put("/otn", cookie);
                                            session.addCookies(cookies);
                                            json = (JSONObject) session.post(url, null, data);
                                            Log.d("getQueueCount", json.toString());
                                            Looper.prepare();
                                            Toast.makeText(BuyActivity.this, json.toString(), Toast.LENGTH_SHORT).show();
                                            Looper.loop();
                                    }


                                    }
                                    Log.d("","");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
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
                    if (ticketsJsonArray.length()!=0) {
                        JSONObject stationMap = ticketInfo.getJSONObject("data").getJSONObject("map");
                        ticketList.clear();
                        for (int i = 0; i < ticketsJsonArray.length(); i++) {
                            Ticket ticket = new Ticket();
                            String[] ticketArray = ticketsJsonArray.getString(i).split("\\|");
                            ticketArrays.add(ticketArray);
                            ticket.setBuyCode(ticketArray[0]);
                            ticket.setTrainId(ticketArray[3]);
                            if (stationCodeName != null) {
                                ticket.setStartStation(stationCodeName.get(ticketArray[6]));
                                ticket.setArrivalStation(stationCodeName.get(ticketArray[7]));
                            } else {
                                ticket.setStartStation(stationMap.getString(ticketArray[6]));
                                ticket.setArrivalStation(stationMap.getString(ticketArray[7]));
                            }
                            ticket.setStartTime(ticketArray[8]);
                            ticket.setArrivalTime(ticketArray[9]);
                            ticket.setThroughTime(ticketArray[10]);

                            ticket.setSpecialSeat(ticketArray[32].equals("") ? "-" : ticketArray[32]);
                            ticket.setLevelOneSeat(ticketArray[31].equals("") ? "-" : ticketArray[31]);
                            ticket.setLevelTwoSeat(ticketArray[30].equals("") ? "-" : ticketArray[30]);
                            ticket.setSeniorSoft(ticketArray[21].equals("") ? "-" : ticketArray[21]);
                            ticket.setLevelOneSoft(ticketArray[23].equals("") ? "-" : ticketArray[23]);
                            ticket.setBulletSoft(ticketArray[33].equals("") ? "-" : ticketArray[33]);
                            ticket.setHardsLeeper(ticketArray[28].equals("") ? "-" : ticketArray[28]);
                            ticket.setSoftSeat("-");
                            ticket.setHardSeat(ticketArray[29].equals("") ? "-" : ticketArray[29]);
                            ticket.setNoSeat(ticketArray[26].equals("") ? "-" : ticketArray[26]);
                            ticket.setOther("-");
                            ticketList.add(ticket);
                        }
                        handler.sendEmptyMessage(1);
                    }else {
                        Looper.prepare();
                        Toast.makeText(BuyActivity.this, "没有查到车次信息,可能改线路暂未开通!", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
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

    @Override
    protected void onPause() {
        super.onPause();
        Session.dump(session, null);
    }
}

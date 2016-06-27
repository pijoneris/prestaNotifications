package com.example.benas.prestanotifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class NotificationActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private boolean visibility = false;
    private RecycleAdapter adapter;
    private ArrayList<InfoHolder> data = new ArrayList<InfoHolder>();
    private boolean isReceiverRegistered;
    private BroadcastReceiver broadcastReceiver;
    private Handler handler;
    public static TextView no_notifs;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_rigth_out);
        setContentView(R.layout.activity_notification);

        SharedPreferences loginPrefs = getSharedPreferences("DataPrefs",MODE_PRIVATE);
        String username = loginPrefs.getString("username", "");
        String password = loginPrefs.getString("password", "");

        new ServerManager(this,"LOGIN").execute("LOGIN", username, password, "0");
        no_notifs = (TextView) findViewById(R.id.no_new_notifications);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(Color.parseColor("#ecf0f1"));
        setSupportActionBar(myToolbar);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                adapter.remove(msg.arg1);

                if(msg.arg1==0){
                    no_notifs.setVisibility(View.VISIBLE);
                }

                super.handleMessage(msg);
            }
        };

        SharedPreferences sharedPreferences = getSharedPreferences("notifications", MODE_PRIVATE);

        String NotificationDataRaw = sharedPreferences.getString("notification_data", "");







        if(!NotificationDataRaw.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(NotificationDataRaw);
                if(jsonArray.length()==0){
                    no_notifs.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String message = "Message: " + jsonObject.getString("message");
                    String type = jsonObject.getString("type");
                    String message_date = jsonObject.getString("message_date");
                    String order_reference = "Order reference: " + jsonObject.getString("order_reference");
                    String buyer_name = "Buyer name: " + jsonObject.getString("buyer_name");
                    String cost = "Cost: " + jsonObject.getString("cost");
                    String order_amount = "Order amount: " + jsonObject.getString("order_amount");
                    String payment_method = "Payment method: " + jsonObject.getString("payment_method");
                    String order_status = "Order status: " + jsonObject.getString("order_status");

                    data.add(new InfoHolder(cost, type, buyer_name, order_amount, message_date, order_reference, order_status, payment_method, true));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        adapter = new RecycleAdapter(this, data);



        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.Callback callback = new RecyclerViewHelper(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                no_notifs.setVisibility(View.INVISIBLE);
                Log.i("TEST", "DAEJOOOOOO");

                    SharedPreferences sharedPreferences = getSharedPreferences("notifications", MODE_PRIVATE);
                    String NotificationDataRaw = sharedPreferences.getString("notification_data", "");

                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(NotificationDataRaw);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = jsonArray.getJSONObject(jsonArray.length()-1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        String message = "Message: " +  jsonObject.getString("message");
                        String type = jsonObject.getString("type");
                        String message_date =  jsonObject.getString("message_date");
                        String order_reference = "Order reference: " + jsonObject.getString("order_reference");
                        String buyer_name = "Buyer name: " + jsonObject.getString("buyer_name");
                        String cost = "Cost: " + jsonObject.getString("cost");
                        String order_amount = "Order amount: " + jsonObject.getString("order_amount");
                        String payment_method = "Payment method: " + jsonObject.getString("payment_method");
                        String order_status = "Order status: " + jsonObject.getString("order_status");
                        adapter.add(new InfoHolder(cost, type, buyer_name, order_amount, message_date, order_reference, order_status, payment_method, true));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


            }
        };

        registerReceiver();

    }



    @Override
    protected void onResume() {
        registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                    new IntentFilter("UPDATE_REQUIRED"));
            isReceiverRegistered = true;
        }
    }

    public void dismissAll(MenuItem item) {


            final Timer t = new Timer();

            t.schedule(new TimerTask() {

                @Override
                public void run() {
                    SharedPreferences sharedPreferences = getSharedPreferences("notifications", MODE_PRIVATE);
                    String NotificationDataRaw = sharedPreferences.getString("notification_data", "");

                    if (!NotificationDataRaw.isEmpty()) {
                        try {


                            JSONArray jsonArray = new JSONArray(NotificationDataRaw);


                            if (jsonArray.length() != 0) {

                                Message message = Message.obtain();
                                message.arg1 = jsonArray.length() - 1;
                                handler.sendMessage(message);
                            }else{
                                t.cancel();
                                t.purge();
                            }

                        } catch (JSONException e) {
                            t.cancel();
                            t.purge();

                            e.printStackTrace();
                        }
                    }
                }
            }, 50, 50);

        }


    public void logout(MenuItem item) {
        new ServerManager(this,"LOGOUT").execute("LOGOUT");
    }
}

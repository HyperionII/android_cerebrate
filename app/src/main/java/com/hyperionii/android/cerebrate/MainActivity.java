package com.hyperionii.android.cerebrate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CerebrateSocketService.IMessageListener {

    private ArrayAdapter<String> messages;
    private CerebrateSocketService cerebrateSocketService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            cerebrateSocketService = ((CerebrateSocketService.Binder)service).getService();

            ArrayList<String> savedMessages = cerebrateSocketService.getMessages();
            MainActivity.this.messages.clear();
            MainActivity.this.messages.addAll(savedMessages);

            cerebrateSocketService.setMessageListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            cerebrateSocketService.setMessageListener(null);
            cerebrateSocketService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("onCreate", "Loading...");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent startServiceIntent = CerebrateSocketService.startServiceIntent(this.getApplicationContext());
        this.startService(startServiceIntent);

        ArrayList<String> list = new ArrayList<>();
        this.messages = new ArrayAdapter<>(this, R.layout.message_layout, list);

        ListView lvMessages = (ListView)findViewById(R.id.messagesList);
        lvMessages.setAdapter(messages);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent startServiceIntent = CerebrateSocketService.startServiceIntent(this.getApplicationContext());
        this.bindService(startServiceIntent, serviceConnection, Context.BIND_IMPORTANT);
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.cerebrateSocketService.setMessageListener(null);
        this.unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onMessage(String message) {
        this.messages.add(message);
        this.messages.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.hyperionii.android.cerebrate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.ArrayList;

public class CerebrateSocketService extends Service {
    private WebSocketClient wsClient;
    private final IBinder mBinder = new Binder();
    private Handler mHandler;
    private IMessageListener messageListener;
    public static final String ACTION_START_SERVICE = "hyperionii.android.cerebrate.ACTION_START_SERVICE";
    public ArrayList<String> messages;

    private WebSocketAdapter socketAdapter = new WebSocketAdapter() {
        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            Log.i("onPingFrame", "ping frame received!");
        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
            Log.i("onPongFrame", "pong frame received!");
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, final boolean closedByServer) throws Exception {
            Log.i("onDisconnected", "Connection lost!");

            // Attempts to restore connection.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Reconnect if is not connected.
                    while(!wsClient.isConnected()) {
                        wsClient.reconnect();

                        // Sleep for wsClient.TIMEOUT * 3 if client is trying to reconnect.
                        while(wsClient.isConnecting()) {
                            try {
                                java.lang.Thread.sleep(wsClient.TIMEOUT * 3);
                            }catch(InterruptedException ex){
                                String message = ex.getMessage();

                                if (message == null) {
                                    message = "Undefined InterruptedException!";
                                }

                                Log.i("WebSocketAdapter", message);
                            }
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onTextMessage(WebSocket webSocket, final String message) {
            Log.i("onTextMessage", message);

            messages.add(message);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (messageListener != null) {
                        messageListener.onMessage(message);
                    } else {
                        createNotification(message);
                    }
                }
            });
        }
    };

    public class Binder extends android.os.Binder {
        CerebrateSocketService getService() {
            return CerebrateSocketService.this;
        }
    }

    public void createNotification(String message) {
        Log.i("CerebrateSocketService", "Sending notification....");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Overmind Message")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;

        ((NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
    }

    public static Intent startServiceIntent(Context context) {
        Intent i = new Intent(context, CerebrateSocketService.class);
        i.setAction(ACTION_START_SERVICE);

        return i;
    }

    public void setMessageListener(IMessageListener messageListener){
        this.messageListener = messageListener;
    }

    public ArrayList<String> getMessages() {
        return this.messages;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("CerebrateSocketService", "onCreate()");
        this.mHandler = new Handler();
        this.messages = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service starting...", Toast.LENGTH_SHORT).show();

        if (intent != null) {
            Log.i("CerebrateSocketService", intent.toUri(0));
        }

        WakeLock wakeLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Cerebrate Service");
        wakeLock.acquire();

        if (wsClient == null) {
            wsClient = new WebSocketClient();
        }

        if (!wsClient.isConnected()) {
            wsClient.connect(this.socketAdapter);
        }

        wakeLock.release();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.wsClient != null) {
            this.wsClient.disconnect();
        }

        Toast.makeText(this, "Service stopping...", Toast.LENGTH_SHORT).show();
    }

    public interface IMessageListener {
        void onMessage(String message);
    }
}

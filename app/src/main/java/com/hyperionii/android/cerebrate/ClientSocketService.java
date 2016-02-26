package com.hyperionii.android.cerebrate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

public class ClientSocketService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private final IBinder mBinder = new Binder();
    private WebSocket ws;

    private static final int TIMEOUT = 5000;
    public static final String SERVER_URL = "10.0.0.2:2222";
    public static final String ACTION_START_SERVICE = "hyperionii.android.cerebrate.ACTION_START_SERVICE";


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i("ServiceHandler", msg.toString());
        }
    }

    public class Binder extends android.os.Binder {
        ClientSocketService getService() {
            return ClientSocketService.this;
        }
    }

    public static Intent startServiceIntent(Context context) {
        Intent i = new Intent(context, ClientSocketService.class);
        i.setAction(ACTION_START_SERVICE);

        return i;
    }

    private void initWebSocket() {
        try {
            Log.i("WebSocketClient", "Connecting to server...");

            ws = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(SERVER_URL)
                    .addListener(new WebSocketAdapter() {

                        @Override
                        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                            Log.i("onPingFrame", "ping frame received!");
                        }

                        @Override
                        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                            Log.i("onPongFrame", "pong frame received!");
                        }

                        @Override
                        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                            Log.i("onDisconnected", "Connection lost!");
                        }

                        @Override
                        public void onTextMessage(WebSocket webSocket, String message) {
                            Log.i("onTextMessage", message);
                        }
                    })
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .connect();

            Log.i("WebSocketClient", "Done!");
            Log.i("WebSocketClient", "Awaiting for messages...");
        } catch(Exception ex) {
            String message = ex.getMessage();

            if (message == null) {
                message = "Unhandled exception!";
            }

            Log.e("WebSocketClient", message);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ClientSocketService", "onCreate()");

        HandlerThread thread = new HandlerThread("ClientSocketServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service starting...", Toast.LENGTH_SHORT).show();

        if (intent != null) {
            Log.i("ClientSocketService", intent.toUri(0));
        }

        WakeLock wakeLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Cerebrate Service");
        wakeLock.acquire();

        // mShutDown = false;

        if (ws == null) {
            this.initWebSocket();
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

        Toast.makeText(this, "Service stopping...", Toast.LENGTH_SHORT).show();
    }
}

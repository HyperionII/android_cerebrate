package com.hyperionii.android.cerebrate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class ClientSocketService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private final IBinder mBinder = new Binder();
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

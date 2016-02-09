package com.hyperionii.android.cerebrate;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.w3c.dom.Text;

import java.io.IOException;

/**
 * Created by aB on 05/02/2016.
 */
public class WebSocketClient extends AsyncTask<Void, String, Object>{

    // Client ws.
    private WebSocket ws;
    // Server's websocket address.
    private static final String SERVER = "ws://10.0.2.2:2222/ws";
    // Timeout value for socket connection.
    private static final int TIMEOUT = 5000;
    // Print server's text.
    private ArrayAdapter<String> messages;
    // Activity attached to
    private Activity activity;

    public WebSocketClient(Activity activity, ArrayAdapter<String> messages) {
        this.activity = activity;
        this.messages = messages;
    }

    @Override
    protected Object doInBackground(Void... params) {
        try {
            Log.i("WebSocketClient", "Connecting to server...");

            ws = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(SERVER)
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
                            publishProgress("Connection lost!");
                        }

                        @Override
                        public void onTextMessage(WebSocket webSocket, String message) {
                            Log.i("onTextMessage", message);
                            publishProgress(message);
                        }
                    })
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .connect();

            Log.i("WebSocketClient", "Done!");
            Log.i("WebSocketClient", "Awaiting for messages...");
            return null;
        } catch(Exception ex) {
            return ex;
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (values.length > 0) {
            String message = values[0];
            this.messages.add(message);

            Context context = activity.getApplicationContext();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            Notification notification = new NotificationCompat.Builder(context)
                    .setContentTitle("Nueva Jugada!")
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
            .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;

            ((NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if (result instanceof Exception) {
            Exception ex = (Exception)result;
            String message = ex.getMessage();
            Log.e("WebSocketClient", "Exception:");
            Log.e("WebSocketClient", ex.toString());

            if (message != null) {
                Log.e("WebSocketClient", message);
            }
        }

        super.onPostExecute(result);
    }
}

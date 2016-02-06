package com.hyperionii.android.cerebrate;

import android.os.AsyncTask;
import android.util.Log;
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
    private TextView chat;

    public WebSocketClient(TextView chat) {
        this.chat = chat;
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
            chat.setText(values[0].toString());
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

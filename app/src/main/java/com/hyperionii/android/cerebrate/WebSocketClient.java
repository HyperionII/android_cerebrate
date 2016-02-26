package com.hyperionii.android.cerebrate;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;

/**
 * Created by aB on 05/02/2016.
 */
public class WebSocketClient{

    private WebSocket ws;
    private Thread createConnThread;
    private static final String SERVER = "ws://10.0.2.2:2222/ws";
    private static final int TIMEOUT = 5000;

    public boolean isConnected() {
        if (ws == null) {
            return false;
        }

        return ws.getState() == WebSocketState.OPEN;
    }

    public synchronized void disconnect() {
        if (this.isConnected()) {
            this.ws.disconnect();
        }
    }

    public synchronized void connect(final WebSocketAdapter socketAdapter) {
        if (this.isConnected()) {
            return;
        }

        this.createConnThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("WebSocketClient", "Connecting to server...");

                    ws = new WebSocketFactory()
                            .setConnectionTimeout(TIMEOUT)
                            .createSocket(SERVER)
                            .addListener(socketAdapter)
                            .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                            .connect();

                    Log.i("WebSocketClient", "Done!");
                    Log.i("WebSocketClient", "Awaiting for messages...");
                } catch(WebSocketException ex) {
                    String message = ex.getMessage();

                    if (message == null) {
                        message = "WebSocketException catched!";
                    }

                    Log.e("WebSocketClient", message);
                } catch (java.io.IOException ex) {
                    String message = ex.getMessage();

                    if (message == null) {
                        message = "Unhandled IO Exception when creating a websocket!";
                    }

                    Log.e("WebSocketClient", message);
                }
            }
        });

        this.createConnThread.start();
    }

}

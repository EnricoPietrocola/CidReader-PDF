package com.artifex.mupdf.mini;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
//import java.nio.charset.StandardCharsets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Document;

public class UDP_Server
{
    private boolean Server_active = true;
    public int port = 12777;

    public String _previousAction = "";
    public String action = "";
    private AsyncTask<Void, Void, Void> async;

    @SuppressLint({"NewApi", "StaticFieldLeak"})
    public void runUdpServer(final Context context)
    {

        async = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                byte[] lMsg = new byte[4096];
                DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
                DatagramSocket ds = null;

                try {
                    ds = new DatagramSocket(port);

                    while (Server_active) {
                        ds.receive(dp);
                        action = new String(lMsg, 0, dp.getLength());
                        //Log.i("tag", "Server Received " + dp.getAddress().toString() + " " + new String(lMsg, StandardCharsets.UTF_8));
                        if(!action.equals(_previousAction)) {
                            Log.i("tag", "Server Received " + dp.getAddress().toString() + "," + action);
                            Intent i = new Intent();
                            i.setAction("Main.MESSAGE_RECEIVED");
                            i.putExtra("Main.MESSAGE_STRING", dp.getAddress().toString() + "," + action);
                            context.getApplicationContext().sendBroadcast(i);
                        }
                        else{
                            //discard doubles
                            //Log.i("tag", "MESSAGE WAS THE SAME " + new String(lMsg, 0, dp.getLength()));
                        }
                        _previousAction = action;

                    }
                } catch (Exception e) {
                    Log.i("tag", "DIO NOT Launch Server");

                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        Log.i("tag", "DIO closing server");

                        ds.close();

                    }
                }

                return null;
            }
        };

        if (Build.VERSION.SDK_INT >= 23) async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async.execute();
    }

    public void stop_UDP_Server()
    {
        Server_active = false;
    }
}
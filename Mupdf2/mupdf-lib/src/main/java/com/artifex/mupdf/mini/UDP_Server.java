package com.artifex.mupdf.mini;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    private AsyncTask<Void, Void, Void> async;
    private boolean Server_aktiv = true;

    @SuppressLint({"NewApi", "StaticFieldLeak"})
    public void runUdpServer(final Context context)
    {
        async = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                byte[] lMsg = new byte[4096];
                DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
                DatagramSocket ds = null;

                try
                {
                    ds = new DatagramSocket(12777);

                    while(Server_aktiv)
                    {
                        ds.receive(dp);
                        Log.i("myServer", "Server Received " + dp.getAddress().toString() + new String(lMsg, "UTF-8"));
                        Intent i = new Intent();
                        i.setAction("Main.MESSAGE_RECEIVED");
                        i.putExtra("Main.MESSAGE_STRING", dp.getAddress().toString() + "," + new String(lMsg, 0, dp.getLength()));
                        context.getApplicationContext().sendBroadcast(i);
                    }
                }
                catch (Exception e)
                {
                    Log.i("tag", "DIO NOT Launch Server");

                    e.printStackTrace();
                }
                finally
                {
                    if (ds != null)
                    {
                        Log.i("tag", "DIO closing server");

                        ds.close();

                    }
                }

                return null;
            }
        };

        if (Build.VERSION.SDK_INT >= 11) async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async.execute();
    }

    public void stop_UDP_Server()
    {
        Server_aktiv = false;
    }
}
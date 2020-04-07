package com.artifex.mupdf.mini;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.net.InetAddress;

public class UDP_Client {
    private AsyncTask<Void, Void, Void> async_cient;
    public static String Message;
    public static InetAddress addr;
    public static int port = 12777;

    @SuppressLint("NewApi")
    public void Send() {

        async_cient = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DatagramSocket ds = null;

                try {
                    //Log.i("tag", "preparing udp");
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    dp = new DatagramPacket(Message.getBytes(), Message.length(), addr, port); //original port was 12777
                    ds.setBroadcast(true);
                    ds.send(dp);
                    //Log.i("tag", "sending data " + Message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        };

        if (Build.VERSION.SDK_INT >= 23)
            async_cient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_cient.execute();
    }


}

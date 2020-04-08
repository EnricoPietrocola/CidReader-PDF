package com.artifex.mupdf.mini;

import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

public class SyncUDP extends AsyncTask<Object, String, String> {

    private String resp;
    //ProgressDialog progressDialog;


    //@Override
    protected String doInBackground(Object... params) {
        publishProgress("Sleeping..."); // Calls onProgressUpdate()
        try {
            int time = (int)params[0];
            String ip = (String)params[1];
            ArrayList<PaintView> paintViews = (ArrayList<PaintView>) params[2];

            for (int i = 0; i < paintViews.size(); i++){
                //create remote paintview with IP HERE
                Log.i("CID", "FOR IP " + paintViews.get(i).ipAddress);

                //get pages
                for(int j = 0; j < paintViews.get(i).actionPages.size(); j++) {
                    //get page
                    for(int l = 0; l < paintViews.get(i).actionPages.get(j).size(); l++){
                        //Log.i("CID", "SyncAction " + paintViews.get(i).actionPages.get(j).get(l));
                        Thread.sleep(time);
                        syncDrawOnScreen(paintViews.get(i).actionPages.get(j).get(l));

                    }
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            resp = e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            resp = e.getMessage();
        }
        //return resp;
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation

    }


    @Override
    protected void onPreExecute() {
    }


    @Override
    protected void onProgressUpdate(String... text) {
    }

    private void syncDrawOnScreen(String action){
        UDP_Client udpClient = new UDP_Client();
        udpClient.addr = DocumentActivity.ipTargetAddress;
        udpClient.port = DocumentActivity.port;
        udpClient.Message = "drawOnScreen," + action;
        udpClient.Send();
    }
}



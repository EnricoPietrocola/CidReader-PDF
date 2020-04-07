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
            /*int secs = 0;
            for(int i = 0; i < paintViews.size(); i++){

                Thread.sleep(time);

                resp = "Slept for " + params[0] + " seconds";
                Log.i("CID", "DOING " + resp + " secs " + secs);
            }*/

            for (int i = 0; i < paintViews.size(); i++){
                //create remote paintview with IP HERE
                Log.i("CID", "FOR IP " + paintViews.get(i).ipAddress);

                //get pages
                for(int j = 0; j < paintViews.get(i).actionPages.size(); j++) {
                    //draw stuff paintViews.get(i).actionPages.get(j)

                    //get page
                    for(int l = 0; l < paintViews.get(i).actionPages.get(j).size(); l++){


                        Log.i("CID", "SyncAction " + paintViews.get(i).actionPages.get(j).get(l));
                        //String[] parsedActionMessage = parseAction(paintViews.get(i).actionPages.get(j).get(l));
								/*for(int y = 0; y < parsedStuff.length; y++){
									Log.i("CID", parsedStuff[y]);
								}*/
                        /*String action = parsedActionMessage[0];
                        int pageNumber = Integer.parseInt(parsedActionMessage[1]);
                        float x = Float.parseFloat(parsedActionMessage[2]);
                        float y = Float.parseFloat(parsedActionMessage[3]);
                        int strokeWidth = Integer.parseInt(parsedActionMessage[4]);
                        int color = Integer.parseInt(parsedActionMessage[5]);
                        boolean isTrail = Boolean.parseBoolean(parsedActionMessage[6]);*/
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


    //@Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation
        //progressDialog.dismiss();
        //finalResult.setText(result);
    }


    @Override
    protected void onPreExecute() {
    }


    @Override
    protected void onProgressUpdate(String... text) {
    }

    public void syncAnnotationsToIp(String ip, ArrayList<PaintView> paintViews){



        for (int i = 0; i < paintViews.size(); i++){
            //create remote paintview with IP HERE
            Log.i("CID", "FOR IP " + paintViews.get(i).ipAddress);

            //get pages
            for(int j = 0; j < paintViews.get(i).actionPages.size(); j++) {
                //draw stuff paintViews.get(i).actionPages.get(j)

                //get page
                for(int l = 0; l < paintViews.get(i).actionPages.get(j).size(); l++){


                    Log.i("CID", "SyncAction " + paintViews.get(i).actionPages.get(j).get(l));
                    //String[] parsedActionMessage = parseAction(paintViews.get(i).actionPages.get(j).get(l));
								/*for(int y = 0; y < parsedStuff.length; y++){
									Log.i("CID", parsedStuff[y]);
								}*/
					/*String action = parsedActionMessage[0];
					int pageNumber = Integer.parseInt(parsedActionMessage[1]);
					float x = Float.parseFloat(parsedActionMessage[2]);
					float y = Float.parseFloat(parsedActionMessage[3]);
					int strokeWidth = Integer.parseInt(parsedActionMessage[4]);
					int color = Integer.parseInt(parsedActionMessage[5]);
					boolean isTrail = Boolean.parseBoolean(parsedActionMessage[6]);*/
                    syncDrawOnScreen(paintViews.get(i).actionPages.get(j).get(l));

                }
            }
        }
    }

    private void syncDrawOnScreen(String action){
        UDP_Client udpClient = new UDP_Client();
        udpClient.addr = DocumentActivity.ipTargetAddress;
        udpClient.port = DocumentActivity.port;
        udpClient.Message = "drawOnScreen," + action;
        udpClient.Send();
    }
}



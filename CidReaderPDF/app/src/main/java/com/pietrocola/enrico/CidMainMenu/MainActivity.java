package com.pietrocola.enrico.CidMainMenu;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.artifex.mupdf.mini.DocumentActivity;
import com.artifex.mupdf.mini.XmlParser;

import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;


public class MainActivity extends AppCompatActivity {
    protected final int PERMISSION_REQUEST = 42;

    public Context mainContext;
    public static LinearLayout item;
    public Uri file;
    public EditText ipText;
    private EditText portText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mainContext = getApplicationContext();
        item = new LinearLayout(mainContext);
        ipText = new EditText(mainContext);
        portText = new EditText(mainContext);
        item.setOrientation(LinearLayout.VERTICAL);
        setContentView(item);


        LinearLayout.LayoutParams labelTextLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        /*//ip input text field
        final TextView ipAddressTitle = new TextView(mainContext);
        ipAddressTitle.setText("IP Address");
        ipAddressTitle.setLayoutParams(labelTextLayoutParams);
        item.addView(ipAddressTitle);

        LinearLayout.LayoutParams ipTextLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ipText.setLayoutParams(ipTextLayoutParams);
        ipText.setText("192.168.1.1");
        item.addView(ipText);

        //port input text field
        final TextView connectionsTitle = new TextView(mainContext);
        connectionsTitle.setText("Port");
        connectionsTitle.setLayoutParams(labelTextLayoutParams);
        item.addView(connectionsTitle);

        LinearLayout.LayoutParams portTextLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        portText.setLayoutParams(portTextLayoutParams);
        portText.setText("12777");
        item.addView(portText);
         */

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    3);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant
            return;
        }

        //documents label and list
        final TextView documentsTitle = new TextView(mainContext);
        documentsTitle.setText("Documents");
        documentsTitle.setLayoutParams(labelTextLayoutParams);
        item.addView(documentsTitle);

        createPDFList();

        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_document_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Some action here
            case R.id.action_about:
                //about stuff
                Log.i("CID", "About");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/EnricoPietrocola/CidReader-PDF"));
                startActivity(browserIntent);
                break;

            /*case R.id.action_settings:
                //about stuff
                Log.i("CID", "Settings");
                break;
             */
            default:
                //Log.i("CID", Integer.toString(item.getItemId()));
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 3 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());

                } else {

                    // permission denied. Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void createPDFList(){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

        File directory = new File(path);
        File[] files = directory.listFiles();

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < files.length; i++)
        {
            String filepath = files[i].getName();

            //allowed file extensions for file list
            if(     filepath.endsWith(".PDF") ||
                    filepath.endsWith(".pdf")||
                    filepath.endsWith(".jpg")||
                    filepath.endsWith(".JPG")||
                    filepath.endsWith(".crxml"))
            {
                names.add(filepath);
            }
        }

        ListAdapter documentsListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);

        final ListView documentsList = new ListView(mainContext);
        LinearLayout.LayoutParams documentsListLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        documentsList.setLayoutParams(documentsListLayoutParams);
        documentsList.setAdapter(documentsListAdapter);
        item.addView(documentsList);

        documentsList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String itemClicked = String.valueOf(parent.getItemAtPosition(position));
                        Toast.makeText(MainActivity.this, itemClicked, Toast.LENGTH_LONG).show();
                        Log.i("CID", "test");

                        if(itemClicked.endsWith(".crxml")){

                            String uri;
                            String project_file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" ;
                            //Log.i("CID", "path is is " + project_file_path + fileName);
                            uri = project_file_path + itemClicked;

                            Log.i("CID", "clicked on " + itemClicked);

                            String _content = readFromFile(mainContext, uri);
                            //_content = readFromFile(mainContext, uri);
                            //Log.i("CID", "file uri " + uri);

                            //Log.i("CID", "file content " + _content);

                            //connectAndOpenPDF(ipText);
                            DocumentActivity.projectFileLocation = uri;
                            startMUPDFActivityFromStringLocation(_content);
                        }
                        else {
                            //connectAndOpenPDF(ipText);
                            startMUPDFActivityFromDownloads(itemClicked);
                        }



                    }
                }
        );
    }

    public void connectAndOpenPDF(EditText ipText){
        if (checkSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.INTERNET)) {
            }

            requestPermissions(new String[]{Manifest.permission.INTERNET},
                    3);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant

            return;
        }
        try {
            DocumentActivity.ipTargetAddress = InetAddress.getByName(ipText.getText().toString());
            DocumentActivity.port = Integer.parseInt(portText.getText().toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //use this function to access data so that when we switch from download folder to a better suited one we have to only change this
    public Uri getUriFromFileName(String fileName){
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, fileName);
        Uri uri = Uri.fromFile(file);
        Log.i("CID", "uri is " + uri);
        return uri;
    }

    public void startMUPDFActivityFromDownloads(String fileName){
        /*File dir = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, fileName);
        Uri uri = Uri.fromFile(file);
        */
        Uri uri = getUriFromFileName(fileName);
        DocumentActivity.fileLocation = uri.toString();

        startMuPDFActivity(uri);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
    }

    public void startMUPDFActivityFromStringLocation(String fileName){
        Uri uri = Uri.parse(new File(fileName).toString());
        DocumentActivity.fileLocation = uri.toString();
        startMuPDFActivity(uri);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
    }

    public void startMuPDFActivity(Uri documentUri) {
        Intent intent = new Intent(this, DocumentActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(documentUri);
        startActivity(intent);
    }

    private static final int READ_REQUEST_CODE = 42;
    /**
     * Fires an intent to spin up the "file chooser" UI
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("application/pdf");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    protected String readFromFile(Context context, String file) {

        String ret = "error";
        Log.i("CID", "readFromFile");

        try {
            //InputStream inputStream = context.openFileInput(file);
            Log.i("CID", "trying to create inputStream");

            FileInputStream inputStream = new FileInputStream(file);
            Log.i("CID", "inputStream created with file " + file);


            if ( inputStream != null ) {
                List data; // = new ArrayList();
                data = XmlParser.parseSession(inputStream);

                Log.i("CID", "appVersion " + data.get(0));
                Log.i("CID", "document " + data.get(1));

                ret = data.get(1).toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return ret;
    }


}

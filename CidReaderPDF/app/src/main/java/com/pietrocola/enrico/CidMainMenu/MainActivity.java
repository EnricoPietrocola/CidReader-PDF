package com.pietrocola.enrico.CidMainMenu;

import android.Manifest;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.artifex.mupdf.mini.DocumentActivity;

import android.content.Intent;
import android.net.Uri;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    protected final int PERMISSION_REQUEST = 42;

    public Context mainContext;
    public static LinearLayout item;
    public Uri file;
    public EditText ipText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mainContext = getApplicationContext();
        item = new LinearLayout(mainContext);
        ipText = new EditText(mainContext);
        item.setOrientation(LinearLayout.VERTICAL);
        setContentView(item);

        //IP INPUT TEXT FIELD
        LinearLayout.LayoutParams ipTextLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ipText.setLayoutParams(ipTextLayoutParams);
        ipText.setText("192.168.1.1");
        item.addView(ipText);

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

        CreatePDFList();
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

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void CreatePDFList(){
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
                    filepath.endsWith(".JPG"))
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

                        ConnectAndOpenPDF(ipText);

                        startMUPDFActivityFromDownloads(itemClicked);
                    }
                }
        );
    }

    public void ConnectAndOpenPDF(EditText ipText){
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
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void startMUPDFActivityFromDownloads(String fileName){
        File dir = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, fileName);
        Uri uri = Uri.fromFile(file);
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
}

package com.pietrocola.enrico.mupdf2;

import android.Manifest;
import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.artifex.mupdf.mini.DocumentActivity;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.content.Context;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    protected final int PERMISSION_REQUEST = 42;

    public Context mainContext;
    public static RelativeLayout item;
    public Uri file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        mainContext = getApplicationContext();
        item = new RelativeLayout(mainContext);
        setContentView(item);



        /*EditText myEditText = new EditText(mainContext); // Pass it an Activity or Context
        myEditText.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)); // Pass two args; must be LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, or an integer pixel value.
        item.addView(myEditText);*/

        /*//mainContext = getApplicationContext();
        final EditText ipAddressInput = new EditText(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.width = 400;
        layoutParams.height = 150;
        layoutParams.topMargin = 150;
        layoutParams.leftMargin = 50;
        ipAddressInput.setLayoutParams(layoutParams);
        item.addView(ipAddressInput);*/


        final EditText ipText = new EditText(mainContext);
        RelativeLayout.LayoutParams ipTextLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ipTextLayoutParams.width = 400;
        ipTextLayoutParams.height = 100;
        ipTextLayoutParams.topMargin = 0;
        ipTextLayoutParams.leftMargin = 0;
        ipText.setLayoutParams(ipTextLayoutParams);
        //addContentView(ipText, ipTextLayoutParams);
        item.addView(ipText);

        /*final Button openButton = new Button(mainContext);
        RelativeLayout.LayoutParams openButtonLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        openButtonLayoutParams.width = 400;
        openButtonLayoutParams.height = 100;
        openButtonLayoutParams.topMargin = 150;
        openButtonLayoutParams.leftMargin = 0;
        openButton.setLayoutParams(openButtonLayoutParams);
        openButton.setText("File");
        //addContentView(openButton, openButtonLayoutParams);
        item.addView(openButton);


        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //performFileSearch();
            }
        });
        */

        final Button connectButton = new Button(mainContext);
        RelativeLayout.LayoutParams connectButtonLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        connectButtonLayoutParams.width = 400;
        connectButtonLayoutParams.height = 100;
        connectButtonLayoutParams.topMargin = 150;
        connectButtonLayoutParams.leftMargin = 0;
        connectButton.setLayoutParams(connectButtonLayoutParams);
        connectButton.setText("Connect");
        //addContentView(connectButton, connectButtonLayoutParams);
        item.addView(connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startMuPDFActivityWithExampleFile();
            }
        });

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

        Log.i("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.i("Files", "Size: "+ files.length);
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++)
        {
            Log.i("Files", "FileName:" + files[i].getName());
            names[i] = files[i].getName();
        }
        ListAdapter documentsListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);

        final ListView documentsList = new ListView(mainContext);
        RelativeLayout.LayoutParams documentsListLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        documentsListLayoutParams.width = 720;
        documentsListLayoutParams.height = 1000;
        documentsListLayoutParams.topMargin = 250;
        documentsListLayoutParams.leftMargin = 0;
        documentsList.setLayoutParams(documentsListLayoutParams);
        documentsList.setAdapter(documentsListAdapter);
        item.addView(documentsList);

        documentsList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String itemClicked = String.valueOf(parent.getItemAtPosition(position));
                        Toast.makeText(MainActivity.this, itemClicked, Toast.LENGTH_LONG).show();
                        startMUPDFActivityFromDownloads(itemClicked);
                    }
                }
        );


        /*startMuPDFActivityWithExampleFile();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        setContentView(R.layout.activity_main);*/

        /*TextView  tv = new TextView(this);
        tv.setText("SA SA SA PROVA");
        //tv.setBackgroundColor(32);
        RelativeLayout LayoutParams layoutParams = new RelativeLayout.LayoutParams(300, 500);
        //layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        //layoutParams.setMargins(int left, int top, int right, int bottom);
        tv.setLayoutParams(layoutParams);
        /*
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(com.artifex.mupdf.mini.R.layout.document_activity, null);
        RelativeLayout item = view.findViewById(com.artifex.mupdf.mini.R.id.mainRelativeLayout);
        item.addView(tv);
        //setContentView(R.layout.activity_main);
        //setContentView(item);*/
    }

    public void testFunc(){
        startMuPDFActivityWithExampleFile();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        setContentView(R.layout.activity_main);
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

    public void startMuPDFActivityWithExampleFile() {
        File dir = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, "example.pdf");
        Uri uri = Uri.fromFile(file);
        startMuPDFActivity(uri);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        //setContentView(R.layout.activity_main);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        InputStream stream;
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = Uri.parse("example.pdf");
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("TAG", "Uri: " + android.net.Uri.parse(uri.toString()));

                File file = new File(uri.getPath());
                try {
                    stream = getContentResolver().openInputStream(uri);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}

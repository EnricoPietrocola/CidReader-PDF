package com.pietrocola.enrico.mupdf2;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.artifex.mupdf.mini.DocumentActivity;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {
    protected final int PERMISSION_REQUEST = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startMuPDFActivityWithExampleFile();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        setContentView(R.layout.activity_main);

        /*TextView  tv = new TextView(this);
        tv.setText("SA SA SA PROVA");
        //tv.setBackgroundColor(32);
        LayoutParams layoutParams=new LayoutParams(300, 500);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        //layoutParams.setMargins(int left, int top, int right, int bottom);
        tv.setLayoutParams(layoutParams);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(com.artifex.mupdf.mini.R.layout.document_activity, null);
        RelativeLayout item = view.findViewById(com.artifex.mupdf.mini.R.id.mainRelativeLayout);
        item.addView(tv);
        //setContentView(R.layout.activity_main);
        //setContentView(item);*/
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
    }
}

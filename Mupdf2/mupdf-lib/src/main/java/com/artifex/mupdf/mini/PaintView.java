package com.artifex.mupdf.mini;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TimerTask;

import static java.lang.System.out;


public class PaintView extends View {

    private static final int PERMISSION_REQUEST = 42;
    public static int BRUSH_SIZE = 5;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_BG_COLOR = Color.TRANSPARENT;
    public InetAddress ipAddress;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    public Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    public int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    public int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    public Bitmap mBitmap;
    protected Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);



    //from pageview
    protected PageView pageView;
    protected int bitmapW, bitmapH;
    protected int canvasW, canvasH;
    protected int scrollX, scrollY;
    protected Scroller scroller;
    protected GestureDetector detector;
    protected ScaleGestureDetector scaleDetector;
    protected float viewScale, minScale, maxScale;
    protected float annotationOffsetX, annotationOffsetY;
    protected int annotationWidth, annotationHeight;
    protected ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    protected FileOutputStream fOut = null;
    protected Integer pageNum;

    //added for multipage support
    public ArrayList<ArrayList<FingerPath>> page = new ArrayList<>();

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

        mEmboss = new EmbossMaskFilter(new float[] {1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
        //Log.e("PaintView", "Constructor" + String.valueOf(currentColor));
        scroller = new Scroller(context);

        //Log.i("PaintView", "WOAH " + offsetX);
    }

    public void init(int width, int height /*DisplayMetrics metrics*/, int pageCount) {
        //int height = metrics.heightPixels;
        //int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
        normal(); //added here to bypass options
        //Log.i("TAG", "INIT");

        //multipage system
        for (int i = 0; i < pageCount; i++){
            page.add(new ArrayList<FingerPath>());
        }
        //Log.i("PaintView"Of(pageCount));, "pages = " + String.valueOf(page.size()));
        //Log.i("PaintView", "pageCount = " + String.value
        //Log.i("PaintView", "init" + String.valueOf(currentColor));
        //initialize, sync with pages from pagecount = 0

    }

    public void scale(float maxW, float maxH, boolean filter) {
        float ratio = Math.min(maxW / mBitmap.getWidth(), maxH / mBitmap.getHeight());
        int width = Math.round((float) ratio * mBitmap.getWidth());
        int height = Math.round((float) ratio * mBitmap.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, width, height, filter);
        mBitmap = newBitmap;
        //return newBitmap;
    }

    public void normal() {
        emboss = false;
        blur = false;
    }

    public void emboss() {
        emboss = true;
        blur = false;
    }

    public void blur() {
        emboss = false;
        blur = true;
    }

    public void clear() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paths.clear();
        normal();
        invalidate();
    }

    long startTime;

    @Override
    protected void onDraw(Canvas canvas) {

        //mCanvas.drawColor(Color.RED);
        canvas.save();
        mBitmap.eraseColor(Color.TRANSPARENT);
        mBitmap.setHasAlpha(true);
        //semi-transparent debug color
        //mBitmap.eraseColor(Color.argb(120, 255, 255, 120));

        startTime = System.currentTimeMillis();

        Iterator<FingerPath> iterator = paths.iterator();

        //for (FingerPath fp : paths) { // paths is the fingerpath array
        while(iterator.hasNext()){
            FingerPath fp = iterator.next();
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss) {
                mPaint.setMaskFilter(mEmboss);
            }
            else if (fp.blur) {
                mPaint.setMaskFilter(mBlur);
            }

            if (fp.isFading){
                fp.time -= 1;
                mPaint.setAlpha((int)fp.time);
                if (fp.time <= 0){
                    iterator.remove(/*fp*/);
                    invalidate();
                }
            }
            mCanvas.drawPath(fp.path, mPaint);
        }


        //canvas.drawColor(Color.RED);
        if(pageView != null){
            canvas.translate(annotationOffsetX, annotationOffsetY);
            canvas.scale(viewScale, viewScale);
        }

        //This part must be extended with proper page resizing of List and data to be functional
        //bitmaps[i] must be overridden if already present, compare it with document page number, otherwise the list becomes impossibly big and page count is lost
        try {
            if(bitmaps.get(pageNum) != null) {
                bitmaps.set(pageNum, mBitmap);
            }
            else{
                //what happens is page is skipped?
                bitmaps.add(mBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //mBitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);

        canvas.drawBitmap(mBitmap,0/*-annotationOffsetX*/, 0 /*-annotationOffsetY*/, mBitmapPaint);
        canvas.restore();
        postInvalidateOnAnimation();

    }



    public void touchStart(float x, float y) {
        mPath = new Path();

        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth,  mPath);
        //fp.isFading =
        /*if(fp.isFading) {
            //fp.time =
        }*/
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

    }

    public void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void touchUp() {
        mPath.lineTo(mX, mY);
        //Log.i("CID", Integer.toString(paths.size()));
        //get the path being drawn
        FingerPath fp = paths.get(paths.size() - 1);
        //fp.time = System.currentTimeMillis() - startTime;
        fp.isFading = DocumentActivity.isTrail;
    }

    public void saveCurrentPage(int currentPage){
        page.set(currentPage, new ArrayList<>(paths)); //_paths
        //Log.i("PaintView", "Saved page " + String.valueOf(currentPage));
    }

    //for each pdf page we create a path array item to record touch interaction (annotation drawings)
    public void changePage(int pageNumber){
        if(page.get(pageNumber) != null){
            pageNum = pageNumber;
            clear();  //This clears pages for turning page effect
            paths = new ArrayList<FingerPath>(page.get(pageNumber));
            invalidate();
        }
        else {
            //Log.i("PaintView","Page is null");
        }

    }

    public void pageViewTransform(PageView pv){
        pageView = pv;
        annotationHeight = pv.bitmapH;
        annotationWidth = pv.bitmapW;
        //Log.i("offset", pv.offsetX + " " + pv.offsetY);
        //Log.i("offset",  "Paintview " + pv.offsetX + " " + pv.offsetY + " " + bitmapW + " " + bitmapH + " " + canvasW + " " + canvasH + " " + pv.viewScale);
        //annotationOffsetX = (pv.canvasW - pv.bitmapW) / 2;
        //annotationOffsetY = (pv.canvasH - pv.bitmapH) / 2;
        annotationOffsetX = pv.offsetX;
        annotationOffsetY = pv.offsetY;
        viewScale = pv.viewScale;
        //annotationOffsetX = pv.offsetX;
        //annotationOffsetY = pv.offsetY;
        //mCanvas.restore();
        invalidate();

        //scale(annotationWidth, annotationHeight, false);
        /*if(annotationWidth != 0){
            mBitmap.setWidth(annotationWidth);
        }
        if (annotationHeight != 0) {
            mBitmap.setHeight(annotationHeight);
        }*/

        //mBitmap.reconfigure(annotationWidth, annotationHeight, pv.bitmap.getConfig());
        //pv.scale(pageView.bitmapW, pageView.bitmapH, false);
        //mCanvas.translate(annotationOffsetX, annotationOffsetY);
    }

    public void deleteLastPath(){

        if(paths.size() > 0) {
            paths.remove(paths.get(paths.size() - 1));
        }
        else{
            //no more undos
            //we could use this part to gray out the undo button
        }
        invalidate();
    }

    public void saveFirstPage(){
        /*if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        }*/
        /*
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
        }*/

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();            new Thread(new Runnable() {
                public void run() {
                    // a potentially time consuming task
                    for (int i = 0; i < bitmaps.size(); i++) {
                        setDrawingCacheEnabled(true);
                        // check the out is not null..
                        //String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CidReader";
                        String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/CidReader/";
                        //String file_path = "/test/";
                        File dir = new File(file_path);

                        Log.i("CID", file_path);

                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        File file = new File(dir, "annotation" + i + ".png");

                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Log.i("CID", file.toString());

                        try {
                            Log.i("CID", "fOut = new FileOutPutStream(file)");
                            fOut = new FileOutputStream(file);
                        } catch (FileNotFoundException e) {
                            Log.e("CID", "something went wrong with fOut = new FileOutputStream(file)");
                            e.printStackTrace();
                        }

                        Log.i("CID", Integer.toString(bitmaps.size()));
                        //bitmaps.get(i).compress(Bitmap.CompressFormat.PNG, 85, fOut);
                        bitmaps.get(i).compress(Bitmap.CompressFormat.PNG, 100, fOut);

                        try {
                            Log.i("CID", "fOut.flush()");
                            fOut.flush();
                        } catch (IOException e) {
                            Log.e("CID", "something went wrong with fOut.flush");
                            e.printStackTrace();
                        }
                        try {
                            Log.i("CID", "fOut.close()");
                            fOut.close();
                        } catch (IOException e) {
                            Log.e("CID", "something went wrong with fOut.close");
                            e.printStackTrace();
                        }

                        /*System.out.println(file);

                        bitmaps.get(i).recycle();
                        System.gc();
                        */

                    }
                    }
            }).start();



        /* Log.i("CID", "SavingPages");

        for (int i = 0; i < 1; i++) {
            setDrawingCacheEnabled(true);
            // check the out is not null..
            //out.flush();

            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhysicsSketchpad";

            File dir = new File(file_path);
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(dir, "sketchpad" + i + ".png");


            try (FileOutputStream out = new FileOutputStream(file)) {
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

    }

    /*public void saveFirstPage(){
        Log.i("CID", "SavingPages");

        for (int i = 0; i < 1; i++) {

            setDrawingCacheEnabled(true);
            // check the out is not null..
            //out.flush();

            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhysicsSketchpad";

            Log.i("CID", file_path);

            File dir = new File(file_path);
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(dir, "sketchpad" + i + ".png");

            Log.i("CID", file.toString());

            try {
                Log.i("CID", "fOut = new FileOutPutStream(file)");
                fOut = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                Log.e("CID", "something went wrong with fOut = new FileOutputStream(file)");
                e.printStackTrace();
            }
            //page.get(i).

            bitmaps.get(i).compress(Bitmap.CompressFormat.PNG, 85, fOut);

            try {
                Log.i("CID", "fOut.flush()");
                fOut.flush();
            } catch (IOException e) {
                Log.e("CID", "something went wrong with fOut.flush");
                e.printStackTrace();

            }

            try {
                Log.i("CID", "fOut.close()");
                fOut.close();
            } catch (IOException e) {
                Log.e("CID", "something went wrong with fOut.close");
                e.printStackTrace();
            }

            System.out.println(file);

            bitmaps.get(i).recycle();
            System.gc();
        }
    }*/
}
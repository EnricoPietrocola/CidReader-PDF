package com.artifex.mupdf.mini;

import android.content.Context;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;


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
    //protected ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    protected FileOutputStream fOut = null;
    protected Integer pageNum;

    //added for multipage support
    public ArrayList<ArrayList<FingerPath>> page = new ArrayList<>();

    //actionpages is a list of lists of strings, it stores actions for each page
    //structure: User/Page/Actions
    // ',' divides action string with parameters
    // ';' divides actions
    public ArrayList<ArrayList<String>> actionPages = new ArrayList<>();



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
        scroller = new Scroller(context);
    }

    public void init(int width, int height /*DisplayMetrics metrics*/, int pageCount) {
        //int height = metrics.heightPixels;
        //int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        //bitmaps = new ArrayList<Bitmap>(pageCount);
        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
        normal(); //added here to bypass options
        //Log.i("TAG", "INIT");

        //multipage system
        for (int i = 0; i < pageCount; i++){
            page.add(new ArrayList<FingerPath>());
            //bitmaps.add(Bitmap.createScaledBitmap(mBitmap, width, height, false));
        }
        initActionPages(pageCount);
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
        canvas.save();
        mBitmap.eraseColor(Color.TRANSPARENT);
        mBitmap.setHasAlpha(true);

        startTime = System.currentTimeMillis();

        Iterator<FingerPath> iterator = paths.iterator();
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
        if(pageView != null){
            canvas.translate(annotationOffsetX, annotationOffsetY);
            canvas.scale(viewScale, viewScale);
        }

        canvas.drawBitmap(mBitmap,0, 0, mBitmapPaint);
        canvas.restore();
        postInvalidateOnAnimation();
    }

    public void touchStart(float x, float y) {


        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth,  mPath);
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

    public void touchUp(boolean isFading) {
        try {
            mPath.lineTo(mX, mY);
            //get the path being drawn
            FingerPath fp = paths.get(paths.size() - 1);
            fp.isFading = isFading;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveCurrentPage(int currentPage){
        page.set(currentPage, new ArrayList<>(paths)); //_paths
    }

    //for each pdf page we create a path array item to record touch interaction (annotation drawings)
    public void changePage(int pageNumber){
        pageNum = pageNumber;

        if(page.get(pageNumber) != null){
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

        annotationOffsetX = pv.offsetX;
        annotationOffsetY = pv.offsetY;
        scrollX = pv.scrollX;
        scrollY = pv.scrollY;

        viewScale = pv.viewScale;

        invalidate();
    }

    public void deleteLastPath(){

        if(paths.size() > 0) {
            paths.remove(paths.get(paths.size() - 1));
        }
        else{
            //no more undos on current page
            //we could use this part to gray out the undo button
        }
        invalidate();
    }

    public void deleteLastPathOnPage(int pageNumber){
        Log.i("CID", "Received undo on page " + pageNumber);
        if(page.get(pageNumber).size() > 0) {
            page.get(pageNumber).remove(page.get(pageNumber).size() - 1);
            Log.i("CID", "TEST");
        }
        else{
            //no more undos
        }
        invalidate();
    }

    public void saveFirstPage(final String id){
        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < page.size(); i++) {
                    setDrawingCacheEnabled(true);
                    String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/CidReader/";
                    File dir = new File(file_path);

                    //Log.i("CID", file_path);

                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File file = new File(dir, "annotation_" + id + "_" + i + ".png");

                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //Log.i("CID", file.toString());

                    try {
                        //Log.i("CID", "fOut = new FileOutPutStream(file)");
                        fOut = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        //Log.e("CID", "something went wrong with fOut = new FileOutputStream(file)");
                        e.printStackTrace();
                    }

                    //Log.i("CID", Integer.toString(bitmaps.size()));


                    Iterator<FingerPath> iterator = page.get(i).iterator();

                    Bitmap _Bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

                    Canvas _canvas = new Canvas(_Bitmap);

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

                        _canvas.drawPath(fp.path, mPaint);
                    }

                    _Bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

                    try {
                        //Log.i("CID", "fOut.flush()");
                        fOut.flush();
                    } catch (IOException e) {
                        //Log.e("CID", "something went wrong with fOut.flush");
                        e.printStackTrace();
                    }
                    try {
                        //Log.i("CID", "fOut.close()");
                        fOut.close();
                    } catch (IOException e) {
                        //Log.e("CID", "something went wrong with fOut.close");
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    protected void writeToFile(final String id, final String folderName) {

        String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + folderName + "/";
        File dir = new File(file_path);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < page.size(); i++) {

            File file = new File(dir, "annotation_" + id + "_" + i + ".txt");

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                fOut = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Iterator<FingerPath> iterator = page.get(i).iterator();

            while(iterator.hasNext()){
                FingerPath fp = iterator.next();
                /*mPaint.setColor(fp.color);
                mPaint.setStrokeWidth(fp.strokeWidth);
                mPaint.setMaskFilter(null);

                if (fp.emboss) {
                    mPaint.setMaskFilter(mEmboss);
                }
                else if (fp.blur) {
                    mPaint.setMaskFilter(mBlur);
                }
                */
                if (fp.isFading){
                    fp.time -= 1;
                    mPaint.setAlpha((int)fp.time);
                    if (fp.time <= 0){
                        iterator.remove(/*fp*/);
                        invalidate();
                    }
                }
                try {
                    fOut.write((fp.path.toString().getBytes()));
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }

            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    protected String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    protected void initActionPages(int pageCount){
        //initialize actionPages, a list of lists of strings, actions log into it for saving/loading/undo functionalities
        for (int i = 0; i < pageCount; i++){
            Log.i("CID", Integer.toString(pageCount));
            actionPages.add(new ArrayList<String>());
        }
    }
}
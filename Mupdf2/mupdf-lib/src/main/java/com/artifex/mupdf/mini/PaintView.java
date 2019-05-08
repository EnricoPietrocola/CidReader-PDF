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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;


public class PaintView extends View {

    public static int BRUSH_SIZE = 5;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_BG_COLOR = Color.TRANSPARENT;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

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
    }

    public void init(DisplayMetrics metrics, int pageCount) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
        normal(); //added here to bypass options
        Log.i("TAG", "INIT");

        //edit for multipage
        for (int i = 0; i < pageCount; i++){
            page.add(new ArrayList<FingerPath>());
        }

        Log.i("PaintView", "pages = " + String.valueOf(page.size()));
        Log.i("PaintView", "pageCount = " + String.valueOf(pageCount));


        //initialize, sync with pages from pagecount = 0
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

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mCanvas.drawColor(backgroundColor);

        for (FingerPath fp : paths) { // paths is the fingerpath array
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            mCanvas.drawPath(fp.path, mPaint);
        }

        //canvas.drawCircle(100, 100, 100, mPaint);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    public void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
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
    }

    public void saveCurrentPage(int currentPage){
        page.set(currentPage, paths);
        Log.i("PaintView", "Saved page " + String.valueOf(currentPage));
    }

    public void changePage(int pageNumber){

        if(page.get(pageNumber) != null){
            clear();

            for (int i = 0; i < page.get(pageNumber).size(); i++){
                //for (int j = 0; j < page.get(pageNumber).; j++){
                    paths.set(i, page.get(pageNumber));
                    paths.get(i).color = page.get(pageNumber).get(i).color;
                    paths.get(i).emboss = page.get(pageNumber).get(i).emboss;
                    paths.get(i).blur = page.get(pageNumber).get(i).blur;
                    paths.get(i).strokeWidth = page.get(pageNumber).get(i).strokeWidth;
                    paths.get(i).path = page.get(pageNumber).get(i).path; //DOESN'T LOAD BECAUSE OF THIS OR BECAUSE THERE IS SOMETHING NOT DRAWING THIS
               // }
            }
            invalidate();
            Log.i("PaintView", "Loaded page " + String.valueOf(pageNumber));
            //Log.i("PaintView", "Loaded path is " + String.valueOf( paths.get(0).path.isEmpty()));

        }
        else {
            Log.i("PaintView","Page is null");
        }

    }

   /* @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                break;
        }



        boolean ret = super.dispatchTouchEvent(event);
        return ret;
    }*/
}
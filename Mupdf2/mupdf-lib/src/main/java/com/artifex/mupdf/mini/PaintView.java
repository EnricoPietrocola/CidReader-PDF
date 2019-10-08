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
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import org.w3c.dom.Document;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;


public class PaintView extends View {

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
    protected float viewScale, minScale, maxScale;
    protected float annotationOffsetX, annotationOffsetY;
    protected int annotationWidth, annotationHeight;
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
        Log.e("PaintView", "Constructor" + String.valueOf(currentColor));
        scroller = new Scroller(context);

        //Log.i("PaintView", "WOAH " + offsetX);
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

        //multipage system
        for (int i = 0; i < pageCount; i++){
            page.add(new ArrayList<FingerPath>());
        }

        //Log.i("PaintView", "pages = " + String.valueOf(page.size()));
        //Log.i("PaintView", "pageCount = " + String.valueOf(pageCount));

        Log.i("PaintView", "init" + String.valueOf(currentColor));
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

    @Override
    protected void onDraw(Canvas canvas) {

        //mCanvas.drawColor(Color.RED);

        canvas.save();
        //mBitmap.eraseColor(Color.BLUE);

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

        //canvas.drawColor(Color.RED);
        if(pageView != null){
            canvas.translate(pageView.offsetX, pageView.offsetY);
            canvas.scale(pageView.viewScale, pageView.viewScale);
        }
        canvas.drawBitmap(mBitmap, -annotationOffsetX, -annotationOffsetY, mBitmapPaint);
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
        page.set(currentPage, new ArrayList<>(paths)); //_paths
        Log.i("PaintView", "Saved page " + String.valueOf(currentPage));
    }

    public void changePage(int pageNumber){
        if(page.get(pageNumber) != null){
            clear();  //This clears pages for turning page effect
            paths = new ArrayList<FingerPath>(page.get(pageNumber));
            invalidate();
        }
        else {
            Log.i("PaintView","Page is null");
        }

    }

    public void pageViewTransform(PageView pv){
        pageView = pv;
        annotationHeight = pv.bitmapH;
        annotationWidth = pv.bitmapW;
        annotationOffsetX = pv.offsetX;
        annotationOffsetY = pv.offsetY;
        //mCanvas.restore();
        Log.i("PaintView", "OOOOOOOOO " + annotationHeight + " " + annotationWidth + " " + annotationOffsetX + " " + annotationOffsetY);
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
}
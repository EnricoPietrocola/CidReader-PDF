package com.artifex.mupdf.mini;

import com.artifex.mupdf.fitz.*;

import android.content.Context;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;


public class PageView extends View implements
	GestureDetector.OnGestureListener,
	ScaleGestureDetector.OnScaleGestureListener

{
	protected DocumentActivity actionListener;

	protected float viewScale, minScale, maxScale;
	protected Bitmap bitmap;
	protected int bitmapW, bitmapH;
	protected int canvasW, canvasH;
	protected int scrollX, scrollY;
	protected Link[] links;
	protected Quad[] hits;
	protected boolean showLinks;

	protected GestureDetector detector;
	protected ScaleGestureDetector scaleDetector;
	protected Scroller scroller;
	protected boolean error;
	protected Paint errorPaint;
	protected Path errorPath;
	protected Paint linkPaint;
	protected Paint hitPaint;

	//my variables
	protected int offsetX, offsetY;
	public boolean isZoomedX = false;
	public boolean isZoomedY = false;
	public boolean isScrollActive = false;

	public PageView(Context ctx, AttributeSet atts) {
		super(ctx, atts);

		scroller = new Scroller(ctx);
		detector = new GestureDetector(ctx, this);
		scaleDetector = new ScaleGestureDetector(ctx, this);

		viewScale = 1f;
		minScale = 1f;
		maxScale = 4f;

		linkPaint = new Paint();
		linkPaint.setARGB(32, 0, 0, 255);

		hitPaint = new Paint();
		hitPaint.setARGB(32, 255, 0, 0);
		hitPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		errorPaint = new Paint();
		errorPaint.setARGB(255, 255, 80, 80);
		errorPaint.setStrokeWidth(5);
		errorPaint.setStyle(Paint.Style.STROKE);

		errorPath = new Path();
		errorPath.moveTo(-100, -100);
		errorPath.lineTo(100, 100);
		errorPath.moveTo(100, -100);
		errorPath.lineTo(-100, 100);

		scaleDetector.setQuickScaleEnabled(false);

	}

	public void setActionListener(DocumentActivity l) {
		actionListener = l;
	}

	public void setError() {
		if (bitmap != null)
			bitmap.recycle();
		error = true;
		links = null;
		hits = null;
		bitmap = null;
		invalidate();
	}

	public void setBitmap(Bitmap b, boolean wentBack, Link[] ls, Quad[] hs) {
		if (bitmap != null)
			bitmap.recycle();
		error = false;
		links = ls;
		hits = hs;
		bitmap = b;
		bitmapW = (int)(bitmap.getWidth() * viewScale);
		bitmapH = (int)(bitmap.getHeight() * viewScale);
		scroller.forceFinished(true);
		scrollX = wentBack ? bitmapW - canvasW : 0;
		scrollY = wentBack ? bitmapH - canvasH : 0;

		actionListener.initializeLocalGraphics();
		invalidate();
	}

	public void resetHits() {
		hits = null;
		invalidate();
	}

	public void onSizeChanged(int w, int h, int ow, int oh) {
		canvasW = w;
		canvasH = h;
		actionListener.onPageViewSizeChanged(w, h);
	}

	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		scaleDetector.onTouchEvent(event);
		return true;
	}

	public boolean onDown(MotionEvent e) {
		scroller.forceFinished(true);
		return true;
	}

	public void onShowPress(MotionEvent e) { }

	public void onLongPress(MotionEvent e) {
		showLinks = !showLinks;
		invalidate();
	}

	public boolean onSingleTapUp(MotionEvent e) {
		boolean foundLink = false;
		float x = e.getX();
		float y = e.getY();
		if (showLinks && links != null) {
			float dx = (bitmapW <= canvasW) ? (bitmapW - canvasW) / 2f : scrollX;
			float dy = (bitmapH <= canvasH) ? (bitmapH - canvasH) / 2f : scrollY;
			float mx = (x + dx) / viewScale;
			float my = (y + dy) / viewScale;
			for (Link link : links) {
				Rect b = link.bounds;
				if (mx >= b.x0 && mx <= b.x1 && my >= b.y0 && my <= b.y1) {
					if (link.uri != null) {
						actionListener.gotoURI(link.uri);
					}
					else if (link.page >= 0) {
						actionListener.gotoPage(link.page);
					}

					foundLink = true;
					break;
				}
			}
		}
		if (!foundLink) {
			float a = canvasW / 20f;
			float b = canvasW - a;
			if (x <= a){
				goBackward();
			}
			if (x >= b){
				goForward();
			}
			if (x > a && x < b) actionListener.toggleUI();
		}
		invalidate();
		return true;
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
		if(isScrollActive) {
			if (bitmap != null) {
				scrollX += (int)dx;
				scrollY += (int)dy;
				scroller.forceFinished(true);
				invalidate();
			}
		}
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float dx, float dy) {
		/*if (bitmap != null) {
			int maxX = bitmapW > canvasW ? bitmapW - canvasW : 0;
			int maxY = bitmapH > canvasH ? bitmapH - canvasH : 0;
			scroller.forceFinished(true);
			scroller.fling(scrollX, scrollY, (int)-dx, (int)-dy, 0, maxX, 0, maxY);
			invalidate();
		}*/
		return true;
	}

	public boolean onScaleBegin(ScaleGestureDetector det) { return true; }


	public float pageFocusX;
	public float pageFocusY;

	public boolean onScale(ScaleGestureDetector det) {
		if (bitmap != null) {
			float focusX = det.getFocusX();
			float focusY = det.getFocusY();
			float scaleFactor = det.getScaleFactor();
			pageFocusX = (focusX + scrollX) / viewScale;
			pageFocusY = (focusY + scrollY) / viewScale;
			viewScale *= scaleFactor;
			if (viewScale < minScale) viewScale = minScale;
			if (viewScale > maxScale) viewScale = maxScale;
			bitmapW = (int)(bitmap.getWidth() * viewScale);
			bitmapH = (int)(bitmap.getHeight() * viewScale);
			scrollX = (int)(pageFocusX * viewScale - focusX);
			scrollY = (int)(pageFocusY * viewScale - focusY);
			scroller.forceFinished(true);
			invalidate();
		}
		return true;
	}

	public void onScaleEnd(ScaleGestureDetector det) { }

	float size = 18f/20f;

	public void goBackward(){
		scroller.forceFinished(true);
		if (scrollY <= 0) {
			if (scrollX <= 0) {
				actionListener.goBackward();

				return;
			}
			//this scrolls the document when tapping on the side of the screen, doesn't work great when editing music sheet so it's been hidden
			//scroller.startScroll(scrollX, scrollY, -canvasW * 9 / 10, bitmapH - canvasH - scrollY, 500);
		} else {
			//scroller.startScroll(scrollX, scrollY, 0, -canvasH * 9 / 10, 250);
		}
		invalidate();
	}

	public void goForward() {
		scroller.forceFinished(true);
		if (scrollY + canvasH >= bitmapH) {
			if (scrollX + canvasW >= bitmapW) {
				actionListener.goForward();
				return;
			}
			//scroller.startScroll(scrollX, scrollY, canvasW * 9 / 10, -scrollY, 500);
		} else {
			//scroller.startScroll(scrollX, scrollY, 0, canvasH * 9 / 10, 250);
		}
		invalidate();
	}

	public void onDraw(Canvas canvas) {
		int x, y;

		if (bitmap == null) {

			if (error) {
				canvas.translate(canvasW / 2f, canvasH / 2f);
				canvas.drawPath(errorPath, errorPaint);
				invalidate();
			}
			return;
		}

		if (scroller.computeScrollOffset()) {
			scrollX = scroller.getCurrX();
			scrollY = scroller.getCurrY();
			invalidate(); /* keep animating */
		}

		if (bitmapW <= canvasW) {
			scrollX = 0;
			isZoomedX = false;

			x = (canvasW - bitmapW) / 2;
		} else {
			isZoomedX = true;
			if (scrollX < 0) scrollX = 0;
			if (scrollX > bitmapW - canvasW) scrollX = bitmapW - canvasW;
			x = -scrollX;
		}

		if (bitmapH <= canvasH) {
			scrollY = 0;
			isZoomedY = false;

			y = (canvasH - bitmapH) / 2;
		} else {
			isZoomedY = true;
			if (scrollY < 0){
				scrollY = 0;
			}
			if (scrollY > bitmapH - canvasH){
				scrollY = bitmapH - canvasH;
			}
			y = -scrollY;
		}

		offsetX = x;
		offsetY = y;
		actionListener.fitPaintViews();
        Log.i("CID", "Page canvasW " + canvasW + " Page canvasH " + canvasH + " bitmapW " + bitmapW + " bitmapH " + bitmapH);
		canvas.translate(x, y);
		canvas.scale(viewScale, viewScale);
		canvas.drawBitmap(bitmap, 0, 0, null);

		if (showLinks && links != null && links.length > 0) {
			for (Link link : links) {
				Rect b = link.bounds;
				canvas.drawRect(b.x0, b.y0, b.x1, b.y1, linkPaint);
			}
		}

		if (hits != null && hits.length > 0)
			for (Quad q : hits)
			{
				Path path = new Path();
				path.moveTo(q.ul_x, q.ul_y);
				path.lineTo(q.ll_x, q.ll_y);
				path.lineTo(q.lr_x, q.lr_y);
				path.lineTo(q.ur_x, q.ur_y);
				path.close();

				canvas.drawPath(path, hitPaint);
			}
	}
}

package com.artifex.mupdf.mini;

import android.graphics.Path;
import android.util.Log;

public class FingerPath {

    public int color;
    public boolean emboss;
    public boolean blur;
    public int strokeWidth;
    public Path path;
    public boolean isFading;
    public long time;


    public FingerPath(int color, boolean emboss, boolean blur, int strokeWidth, Path path) {
        this.color = color;
        this.emboss = emboss;
        this.blur = blur;
        this.strokeWidth = strokeWidth;
        this.path = path;
        this.isFading = false;
        this.time = 255;
    }
}
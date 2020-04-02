package com.artifex.mupdf.mini;

public class Entry {
    public final String document;
    public final String link;
    public final String summary;

    public Entry(String title, String summary, String link) {
        this.document = title;
        this.summary = summary;
        this.link = link;
    }
}
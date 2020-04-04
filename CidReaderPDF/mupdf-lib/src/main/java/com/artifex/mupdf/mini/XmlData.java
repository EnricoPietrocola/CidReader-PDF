package com.artifex.mupdf.mini;

public class XmlData {
    public final String document;
    public final String link;
    public final String summary;

    public XmlData(String title, String summary, String link) {
        this.document = title;
        this.summary = summary;
        this.link = link;
    }
}
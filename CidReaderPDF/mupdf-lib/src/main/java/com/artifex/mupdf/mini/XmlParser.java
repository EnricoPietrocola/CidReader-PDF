package com.artifex.mupdf.mini;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XmlParser {
        // We don't use namespaces
        private static final String ns = null;

        public static List parse(InputStream in) throws XmlPullParserException, IOException {
            try {

                XmlPullParser parser = Xml.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                //Log.i("CID", documentLocation);
                return readFeed(parser);
                //return null; //readText(parser);
            } finally {
                in.close();
            }
        }

    private static List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        //List entries = new ArrayList();

        //parser.require(XmlPullParser.START_TAG, ns, "root");

        //parser.require(XmlPullParser.START_TAG, ns, "document");
        //String documentLocation = readText(parser);
        //parser.require(XmlPullParser.END_TAG, ns, "document");
        String document = null;
        String appVersion = null;
        String annotation = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.i("CID", "I have " + name);

            // Starts by looking for the entry tag
            if (name.equals("appVersion")) {
                appVersion = readText(parser);
            }
            else if (name.equals("document")) {
                document = readText(parser);
            }
            else if(name.equals("annotation")){
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    name = parser.getName();

                    if (name.equals("user")) {
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            name = parser.getName();
                            Log.i("CID", "Now we have " + name);

                            if (name.equals("address")) {
                                Log.i("CID", readText(parser));
                            }
                            else if(name.equals("page")){
                                Log.i("CID", "page number " + parser.getAttributeValue(ns, "pageNumber"));
                                while (parser.next() != XmlPullParser.END_TAG) {
                                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                                        continue;
                                    }
                                    name = parser.getName();
                                    Log.i("CID", "Now we have " + name);
                                    if(name.equals("path")){
                                        Log.i("CID", "path " + readText(parser));
                                    }
                                    else{
                                        skip(parser);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                skip(parser);
            }
        }
        //return entries;
        Log.i("CID", "appVersion is : " + appVersion);
        Log.i("CID", "Document is here: " + document);
        return null;
        }


    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private static Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {

            Log.i("CID", "is this called? DOCUMENT");
        parser.require(XmlPullParser.START_TAG, ns, "annotation");
        String document = null;
        String summary = "test";//null;
        String link = "test"; // null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("address")) {
                //document = readTitle(parser);
                //Log.i("CID", "DOCUMENT IS " + document);
            } else if (name.equals("appVersion")) {
                //summary = readSummary(parser);
            } else if (name.equals("annotation")) {
                //link = readLink(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(document, summary, link);
    }

    // For the tags title and summary, extracts their text values.
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }



/*// Processes title tags in the feed.
    private static String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "document");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "document");
        return title;
    }*/



    // Processes link tags in the feed.
   /* private static String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        String link = "";
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (tag.equals("link")) {
            if (relType.equals("alternate")){
                link = parser.getAttributeValue(null, "href");
                parser.nextTag();
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }*/




    //potentially useless stuff

    // Processes summary tags in the feed.
    /*private static String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "summary");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "summary");
        return summary;
    }*/



}

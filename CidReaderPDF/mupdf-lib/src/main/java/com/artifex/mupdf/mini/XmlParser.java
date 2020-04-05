package com.artifex.mupdf.mini;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class XmlParser {
        // We don't use namespaces
        private static final String ns = null;

        public static ArrayList<String> parseSession(InputStream in) throws XmlPullParserException, IOException {
            try {

                XmlPullParser parser = Xml.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                //Log.i("CID", documentLocation);
                return readSessionProject(parser);
            } finally {
                in.close();
            }
        }

    //read pdf document location and application version it was created with
    private static ArrayList<String> readSessionProject(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<String> data = new ArrayList();

        String document = null;
        String appVersion = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("appVersion")) {
                appVersion = readText(parser);
                data.add(appVersion);
            } else if (name.equals("document")) {
                document = readText(parser);
                data.add(document);
            } else {
                skip(parser);
            }
        }
        Log.i("CID", "appVersion is : " + appVersion);
        Log.i("CID", "Document is here: " + document);
        //return entries;
        return data;
    }

    public static ArrayList<ArrayList<String>> parseSessionData(InputStream in) throws XmlPullParserException, IOException {
        try {

            XmlPullParser parser = Xml.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            //Log.i("CID", documentLocation);
            return readSessionData(parser);
        } finally {
            in.close();
        }
    }

    //read session annotation data
    private static ArrayList<ArrayList<String>> readSessionData(XmlPullParser parser) throws XmlPullParserException, IOException {

        //a page is an array of actions, a paintview
        ArrayList<ArrayList<String>> annotationData = new ArrayList<>();

        /*for (int i = 0; i < pageCount; i++){
            Log.i("CID", Integer.toString(pageCount));
            annotationData.add(new ArrayList<String>());
        }*/
        parser.require(XmlPullParser.START_TAG, ns, "root");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            //Log.i("CID", "1 loop " + tag);
            // Starts by looking for the entry tag
            if(tag.equals("annotation")){

                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    tag = parser.getName();

                    //Log.i("CID", "2 loop " + tag);


                    if (tag.equals("user")) {   //a user is a paintview in cidreader, both local or remote

                        while (parser.next() != XmlPullParser.END_TAG) {

                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            tag = parser.getName();
                            //Log.i("CID", "3 loop " + tag);

                            if (tag.equals("address")) {
                                readText(parser);
                            }

                            else if(tag.equals("page")){  //access page, based on attribute "pageNumber", choose which annotation page to fill with paths using local/remoteDrawOnScreen

                                //initialize page data
                                ArrayList<String> pageData = new ArrayList<>();

                                //Log.i("CID", "page number " + parser.getAttributeValue(ns, "pageNumber"));

                                while (parser.next() != XmlPullParser.END_TAG) {
                                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                                        continue;
                                    }
                                    tag = parser.getName();

                                    //Log.i("CID", "4 loop " + tag);
                                    //Log.i("CID", tag + readText(parser));


                                    if(tag.equals("action")){ //single action added to actionlist
                                        //Log.i("CID", tag + " " + readText(parser));

                                        pageData.add(readText(parser));
                                        //Log.i("CID", pageData.get(pageData.size() - 1));
                                    }
                                    else{
                                        skip(parser);
                                    }

                                }

                                annotationData.add(pageData);
                            }
                           /*else{
                                skip(parser);
                            }*/
                        }

                    }
                    /*else{
                        skip(parser);
                    }*/
                }

            }
            else {
                skip(parser);
            }
        }
        return annotationData;
    }

    public static ArrayList<String> parseSessionAddresses(InputStream in) throws XmlPullParserException, IOException {
        try {

            XmlPullParser parser = Xml.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            //Log.i("CID", documentLocation);
            return readSessionAddresses(parser);
        } finally {
            in.close();
        }
    }

    private static ArrayList<String> readSessionAddresses(XmlPullParser parser) throws XmlPullParserException, IOException {

        //a page is an array of actions, a paintview
        ArrayList<String> addresses = new ArrayList<>();

        /*for (int i = 0; i < pageCount; i++){
            Log.i("CID", Integer.toString(pageCount));
            annotationData.add(new ArrayList<String>());
        }*/

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            // Starts by looking for the entry tag
            if(name.equals("annotation")){
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    name = parser.getName();

                    if (name.equals("user")) {   //a user is a paintview in cidreader, both local or remote
                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            name = parser.getName();
                            Log.i("CID", readText(parser));

                            if (name.equals("address")) {
                                addresses.add(readText(parser));
                                Log.i("CID", readText(parser));
                            }
                        }
                    }
                }
            }
            else {
                skip(parser);
            }
        }
        return addresses;
    }

        // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
   /* private static XmlData readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {

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
        return new XmlData(document, summary, link);
    }*/

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
            //Log.i("CID", "depth "+ depth + " tag " + parser.getName());
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

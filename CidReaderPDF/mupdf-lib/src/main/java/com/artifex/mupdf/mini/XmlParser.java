package com.artifex.mupdf.mini;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class XmlParser {
        // We don't use namespaces
        private static final String ns = null;

        public static List parse(InputStream in) throws XmlPullParserException, IOException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                //parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                //parser.setInput(in, null);
                //parser.nextTag();
                return readFeed(parser);
            } finally {
                in.close();
            }
        }




        private static List readFeed(XmlPullParser parser){
                Log.i("CID", "Parsed " + parser.toString());
                return null;
        }



}

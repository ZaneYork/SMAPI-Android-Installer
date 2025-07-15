package com.reandroid.xml.base;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public interface XmlReader {
    void parse(XmlPullParser parser) throws XmlPullParserException, IOException;
}

 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.arsc.chunk.xml;

import org.xmlpull.v1.XmlPullParser;

public class ParserEvent {
    private final int event;
    private final ResXmlNode xmlNode;
    private final String comment;
    private final boolean endComment;
    public ParserEvent(int event, ResXmlNode xmlNode, String comment, boolean endComment){
        this.event = event;
        this.xmlNode = xmlNode;
        this.comment = comment;
        this.endComment = endComment;
    }
    public ParserEvent(int event, ResXmlNode xmlNode){
        this(event, xmlNode, null, false);
    }
    public int getEvent() {
        return event;
    }
    public ResXmlNode getXmlNode() {
        return xmlNode;
    }
    public String getComment() {
        return comment;
    }
    public boolean isEndComment() {
        return endComment;
    }

    public static final int START_DOCUMENT = XmlPullParser.START_DOCUMENT;
    public static final int END_DOCUMENT = XmlPullParser.END_DOCUMENT;
    public static final int START_TAG = XmlPullParser.START_TAG;
    public static final int END_TAG = XmlPullParser.END_TAG;
    public static final int TEXT = XmlPullParser.TEXT;
    public static final int CDSECT = XmlPullParser.CDSECT;
    public static final int ENTITY_REF = XmlPullParser.ENTITY_REF;
    public static final int IGNORABLE_WHITESPACE = XmlPullParser.IGNORABLE_WHITESPACE;
    public static final int PROCESSING_INSTRUCTION = XmlPullParser.PROCESSING_INSTRUCTION;
    public static final int COMMENT = XmlPullParser.COMMENT;
    public static final int DOCDECL = XmlPullParser.DOCDECL;

}

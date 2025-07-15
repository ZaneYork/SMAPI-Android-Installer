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
package com.reandroid.xml;

import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.xml.base.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

public abstract class XMLNode implements Node {

    private XMLNode mParent;
    private int mLineNumber;
    private int mColumnNumber;
    public XMLNode(){

    }
    int getLength(){
        return 0;
    }
    int getTextLength(){
        return 0;
    }
    abstract XMLNode newCopy(XMLNode parent);
    public XMLNode getParentNode(){
        return mParent;
    }
    public XMLNode getRootParentNode(){
        XMLNode parent = getParentNode();
        if(parent != null){
            return parent.getRootParentNode();
        }
        return this;
    }
    public Iterator<XMLNode> iterator(){
        return EmptyIterator.of();
    }
    public void removeSelf(){
        XMLNode parent = getParentNode();
        if(parent instanceof XMLNodeTree){
            ((XMLNodeTree) parent).remove(this);
        }
    }
    void setParentNode(XMLNode parent){
        if(parent != this){
            this.mParent = parent;
        }
    }

    public int getColumnNumber() {
        return mColumnNumber;
    }
    public void setColumnNumber(int columnNumber) {
        this.mColumnNumber = columnNumber;
    }
    public int getLineNumber() {
        return mLineNumber;
    }
    public void setLineNumber(int lineNumber) {
        this.mLineNumber = lineNumber;
    }

    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
    }
    abstract void write(Appendable writer, boolean xml, boolean escapeXmlText) throws IOException;
    public String toText(boolean xml, boolean escapeXmlText){
        StringWriter writer = new StringWriter();
        try {
            write(writer, xml, escapeXmlText);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    public String getDebugText(){
        StringWriter writer = new StringWriter();
        try {
            appendDebugText(writer, DEBUG_STRING_LENGTH, 0);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    int appendDebugText(Appendable appendable, int limit, int length) throws IOException {
        return 0;
    }
    @Override
    public String toString(){
        return getDebugText();
    }

    private static final int DEBUG_STRING_LENGTH = 250;
}

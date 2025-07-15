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

import com.reandroid.xml.base.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XMLText extends XMLNode implements Text {
    private String text;
    public XMLText(String text){
        this.text = text;
    }
    public XMLText(){
        this(null);
    }

    @Override
    public XMLElement getParentNode() {
        return (XMLElement) super.getParentNode();
    }

    @Override
    XMLNode newCopy(XMLNode parent) {
        XMLText xmlText;
        if(parent instanceof XMLNodeTree){
            XMLNodeTree nodeTree =  (XMLNodeTree) parent;
            xmlText = nodeTree.newText();
            xmlText.setText(this.text);
            nodeTree.add(xmlText);
        }else {
            xmlText = new XMLText(this.text);
        }
        return xmlText;
    }

    public String getText(){
        return getText(false);
    }
    public String getText(boolean escapeXmlChars){
        if(escapeXmlChars){
            return XMLUtil.escapeXmlChars(text);
        }
        return text;
    }
    public void setText(String text){
        this.text = text;
    }

    public void appendText(char ch) {
        if(ch == 0){
            return;
        }
        appendText(String.valueOf(ch));
    }
    public void appendText(String text) {
        if(text == null){
            return;
        }
        if(this.text == null || this.text.length() == 0){
            this.text = text;
            return;
        }
        this.text = this.text + text;
    }
    private void appendEntityRef(String entityRef) {
        if(entityRef == null){
            return;
        }
        String decode;
        if(entityRef.equals("lt")){
            decode = "<";
        }else if(entityRef.equals("gt")){
            decode = ">";
        }else if(entityRef.equals("amp")){
            decode = "&";
        }else if(entityRef.equals("quote")){
            decode = "\"";
        }else {
            return;
        }
        appendText(decode);
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.text(getText());
    }
    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        int event = parser.getEventType();
        if(!isTextEvent(event)){
            throw new XmlPullParserException("Not text event");
        }
        while (isTextEvent(event)){
            if(event == XmlPullParser.TEXT){
                appendText(parser.getText());
            }else if(event == XmlPullParser.ENTITY_REF){
                appendEntityRef(parser.getName());
            }
            event = parser.next();
        }
    }
    @Override
    void write(Appendable appendable, boolean xml, boolean escapeXmlText) throws IOException {
        String text = getText(escapeXmlText);
        if(text != null){
            appendable.append(text);
        }
    }
    @Override
    int appendDebugText(Appendable appendable, int limit, int length) throws IOException {
        if(length >= limit){
            return length;
        }
        String text = getText();
        if(text != null){
            appendable.append(text);
            length = length + text.length();
        }
        return length;
    }

    boolean isIndent(){
        return isIndentText(getText());
    }

    static boolean isTextEvent(int event){
        return event == XmlPullParser.TEXT
                || event == XmlPullParser.ENTITY_REF;
    }
    private static boolean isIndentText(String text){
        if(android.text.TextUtils.isEmpty(text)){
            return true;
        }
        if(text.charAt(0) != '\n'){
            return false;
        }
        char[] chars = text.toCharArray();
        for(int i = 1; i < chars.length; i++){
            if(chars[i] != ' ' && chars[i] != '\n'){
                return false;
            }
        }
        return true;
    }
}

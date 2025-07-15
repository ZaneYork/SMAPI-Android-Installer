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

import com.reandroid.xml.base.Comment;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class XMLComment extends XMLNode implements Comment {
    private String text;
    public XMLComment(String text){
        this();
        setText(text);
    }
    public XMLComment(){
        super();
    }

    @Override
    XMLComment newCopy(XMLNode parent) {
        XMLComment comment = new XMLComment();
        comment.setText(getText());
        return comment;
    }

    public void setText(String text){
        this.text = text;
    }
    public String getText(){
        return getText(false);
    }
    public String getText(boolean escapeXmlText){
        String text = this.text;
        if(escapeXmlText){
            return XMLUtil.escapeXmlChars(text);
        }
        return text;
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.comment(getText(false));
    }
    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        if(parser.getEventType() != XmlPullParser.COMMENT){
            throw new XmlPullParserException("Invalid event, expecting COMMENT but found "
                    + parser.getEventType());
        }
        setText(parser.getText());
        parser.next();
    }
    @Override
    void write(Appendable appendable, boolean xml, boolean escapeXmlText) throws IOException {
        appendable.append("<!-- ");
        appendable.append(getText(escapeXmlText));
        appendable.append(" -->");
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
}

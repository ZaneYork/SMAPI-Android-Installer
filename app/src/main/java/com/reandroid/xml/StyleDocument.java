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

import com.reandroid.utils.collection.InstanceIterator;
import com.reandroid.utils.io.IOUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

public class StyleDocument extends XMLDocument implements
        SpanSet<StyleElement>, StyleNode, Comparable<StyleDocument>{
    public StyleDocument(){
        super();
    }
    public boolean hasElements(){
        return getElements().hasNext();
    }
    public Iterator<StyleElement> getElements(){
        return iterator(StyleElement.class);
    }

    @Override
    public Iterator<StyleElement> getSpans() {
        return InstanceIterator.of(recursiveNodes(), StyleElement.class);
    }
    // keep
    public Iterator<StyleText> getStyleTexts() {
        return InstanceIterator.of(recursiveNodes(), StyleText.class);
    }

    @Override
    public void appendChar(char ch) {
        if(ch == 0){
            return;
        }
        XMLNode xmlNode = getLast();
        StyleText styleText;
        if(xmlNode instanceof StyleText){
            styleText = (StyleText) xmlNode;
        }else {
            styleText = new StyleText();
            add(styleText);
        }
        styleText.appendChar(ch);
    }
    @Override
    public StyleNode getParentStyle() {
        return null;
    }
    @Override
    public void addStyleNode(StyleNode styleNode){
        add((XMLNode) styleNode);
    }

    public String getXml(){
        return toText(true, false);
    }
    public String getXml(boolean escapeXmlText){
        return toText(true, escapeXmlText);
    }
    public String getHtml(){
        return getText(false, false);
    }
    public String getText(boolean xml, boolean escapeXmlText){
        return toText(xml, escapeXmlText);
    }
    public String getStyledString(){
        StringWriter writer = new StringWriter();
        try {
            writeStyledText(writer);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    void writeStyledText(Appendable appendable) throws IOException {
        Iterator<XMLNode> iterator = iterator();
        while (iterator.hasNext()){
            XMLNode xmlNode = iterator.next();
            if(xmlNode instanceof StyleText){
                StyleText styleText = (StyleText) xmlNode;
                styleText.writeStyledText(appendable);
            } else if(xmlNode instanceof StyleElement){
                StyleElement element = (StyleElement) xmlNode;
                element.writeStyledText(appendable);
            }
        }
    }
    public void parseString(String xmlString) throws XmlPullParserException, IOException {
        xmlString = "<parser>" + xmlString + "</parser>";
        XmlPullParser parser = PARSER;
        parser.setInput(new StringReader(xmlString));
        parseInner(parser);
        IOUtil.close(parser);
    }
    @Override
    void write(Appendable appendable, boolean xml, boolean escapeXmlText) throws IOException {
        appendDocument(appendable, xml);
        appendChildes(iterator(), appendable, xml, escapeXmlText);
    }
    private void appendChildes(Iterator<XMLNode> iterator, Appendable appendable, boolean xml, boolean escapeXmlText) throws IOException {
        while (iterator.hasNext()){
            iterator.next().write(appendable, xml, escapeXmlText);
        }
    }
    @Override
    public int compareTo(StyleDocument document) {
        if(document == null){
            return 0;
        }
        return getStyledString().compareTo(document.getStyledString());
    }
    @Override
    public int hashCode(){
        return getXml().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(!(obj instanceof StyleDocument)){
            return false;
        }
        return getXml().equals(((StyleDocument)obj).getXml());
    }

    @Override
    public StyleElement newElement(){
        return new StyleElement();
    }
    @Override
    public StyleText newText(){
        return new StyleText();
    }
    @Override
    public XMLComment newComment(){
        return null;
    }
    @Override
    public StyleAttribute newAttribute(){
        return new StyleAttribute();
    }

    public static StyleDocument parseNext(XmlPullParser parser) throws IOException, XmlPullParserException {
        StyleDocument styleDocument = new StyleDocument();
        styleDocument.parseInner(parser);
        return styleDocument;
    }
    public static StyleDocument copyInner(XMLElement xmlElement){
        StyleDocument styleDocument = new StyleDocument();
        Iterator<XMLNode> iterator = xmlElement.iterator();
        while (iterator.hasNext()){
            XMLNode xmlNode = iterator.next();
            if(xmlNode instanceof XMLElement){
                StyleElement styleElement = new StyleElement();
                styleDocument.add(styleElement);
                styleElement.copyFrom((XMLElement) xmlNode);
            }else if(xmlNode instanceof XMLText){
                XMLText xmlText = (XMLText)xmlNode;
                StyleText styleText = new StyleText(xmlText.getText());
                styleDocument.add(styleText);
            }
        }
        return styleDocument;
    }

    public static StyleDocument parseStyledString(String xmlStyledString) throws XmlPullParserException, IOException {
        StyleDocument styleDocument = new StyleDocument();
        styleDocument.parseString(xmlStyledString);
        return styleDocument;
    }

    private static final XmlPullParser PARSER = XMLFactory.newPullParser();
}

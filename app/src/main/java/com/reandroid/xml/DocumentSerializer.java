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

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class DocumentSerializer implements XmlSerializer {
    private XMLDocument document;
    private XMLElement currentElement;
    private XMLText currentText;
    private final Map<String, String> namespaceMap;
    private final Map<String, Object> propertiesMap;
    private final Map<String, Boolean> featureMap;

    public DocumentSerializer(XMLDocument document){
        this.document = document;
        this.namespaceMap = new HashMap<>();
        this.propertiesMap = new HashMap<>();
        this.featureMap = new HashMap<>();
    }
    public void setDocument(XMLDocument document) {
        this.document = document;
    }
    @Override
    public void setFeature(String name, boolean state) throws IllegalArgumentException, IllegalStateException {
        featureMap.put(name, state);
    }

    @Override
    public boolean getFeature(String name) {
        Boolean value = this.featureMap.get(name);
        if(value != null){
            return value;
        }
        return false;
    }

    @Override
    public void setProperty(String name, Object value) throws IllegalArgumentException, IllegalStateException {
        propertiesMap.put(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return propertiesMap.get(name);
    }

    @Override
    public void setOutput(OutputStream os, String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new IOException("XMLDocument serializer");
    }

    @Override
    public void setOutput(Writer writer) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new IOException("XMLDocument serializer");
    }

    @Override
    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        this.document.setEncoding(encoding);
        this.document.setStandalone(standalone);
        this.currentElement = null;
        this.currentText = null;
    }

    @Override
    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        this.currentElement = null;
        this.currentText = null;
        this.namespaceMap.clear();
        this.propertiesMap.clear();
    }

    @Override
    public void setPrefix(String prefix, String namespace) throws IOException, IllegalArgumentException, IllegalStateException {
        namespaceMap.put(namespace, prefix);
    }

    @Override
    public String getPrefix(String namespace, boolean generatePrefix) throws IllegalArgumentException {
        if(namespace == null || namespace.length() == 0){
            return null;
        }
        Map<String, String> map = this.namespaceMap;
        String prefix = map.get(namespace);
        if(prefix != null || !generatePrefix){
            return prefix;
        }
        for(int i = 0; i < 1000; i++){
            prefix = "ns" + i;
            if(map.containsValue(prefix)){
                continue;
            }
            map.put(namespace, prefix);
            return prefix;
        }
        return null;
    }

    @Override
    public int getDepth() {
        XMLElement element = this.currentElement;
        if(element != null){
            return element.getDepth();
        }
        return 0;
    }

    @Override
    public String getNamespace() {
        XMLElement element = this.currentElement;
        if(element != null){
            XMLNamespace namespace = element.getNamespace();
            if(namespace != null){
                return namespace.getUri();
            }
        }
        return null;
    }

    @Override
    public String getName() {
        XMLElement element = this.currentElement;
        if(element != null){
            return element.getName(false);
        }
        return null;
    }

    @Override
    public XmlSerializer startTag(String namespace, String name) throws IOException {
        this.currentText = null;
        XMLNodeTree nodeTree = this.currentElement;
        if(nodeTree == null){
            nodeTree = this.document;
        }
        XMLElement element = nodeTree.newElement();
        nodeTree.add(element);
        element.setName(name);
        this.currentElement = element;
        return this;
    }

    @Override
    public XmlSerializer attribute(String namespace, String name, String value) throws IOException{
        XMLAttribute attribute = this.currentElement.newAttribute();
        attribute.set(name, value);
        this.currentElement.addAttribute(attribute);
        attribute.setNamespace(namespace, getPrefix(namespace, true));
        return this;
    }

    @Override
    public XmlSerializer endTag(String namespace, String name) throws IOException{
        XMLElement current = this.currentElement;
        if(current == null){
            throw new IOException("Invalid state endTag " + name);
        }
        if(!current.equalsName(name)){
            throw new IOException("Mismatch end: " + name + ", expect = " + current.getName());
        }
        this.currentElement = current.getParentElement();
        this.currentText = null;
        return this;
    }

    @Override
    public XmlSerializer text(String text) throws IOException{
        appendText(text);
        return this;
    }

    @Override
    public XmlSerializer text(char[] buf, int start, int len) throws IOException, IllegalArgumentException, IllegalStateException {
        StringBuilder builder = new StringBuilder();
        int end = start + len;
        for(int i = start; i < end; i++){
            builder.append(buf[i]);
        }
        appendText(builder.toString());
        return this;
    }

    @Override
    public void cdsect(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void entityRef(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        appendText(XMLUtil.decodeEntityRef(text));
    }

    @Override
    public void processingInstruction(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void comment(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        XMLNodeTree nodeTree = getCurrentNode();
        XMLComment xmlComment = nodeTree.newComment();
        nodeTree.add(xmlComment);
        xmlComment.setText(text);
    }

    @Override
    public void docdecl(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void ignorableWhitespace(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void flush() throws IOException {

    }

    private XMLNodeTree getCurrentNode(){
        XMLNodeTree nodeTree = this.currentElement;
        if(nodeTree == null){
            nodeTree = this.document;
        }
        return nodeTree;
    }
    private void appendText(String text){
        XMLText xmlText = this.currentText;
        if(xmlText == null){
            xmlText = this.document.newText();
            XMLNodeTree nodeTree = getCurrentNode();
            nodeTree.add(xmlText);
            this.currentText = xmlText;
        }
        xmlText.appendText(text);
    }
}

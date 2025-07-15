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

public class XmlSerializerWrapper implements XmlSerializer {

    private final XmlSerializer baseSerializer;

    public XmlSerializerWrapper(XmlSerializer baseSerializer) {
        this.baseSerializer = baseSerializer;
    }

    public XmlSerializer getBaseSerializer() {
        return baseSerializer;
    }

    @Override
    public void setFeature(String name, boolean state) throws IllegalArgumentException, IllegalStateException {
        getBaseSerializer().setFeature(name, state);
    }
    @Override
    public boolean getFeature(String name) {
        return getBaseSerializer().getFeature(name);
    }
    @Override
    public void setProperty(String name, Object value) throws IllegalArgumentException, IllegalStateException {
        getBaseSerializer().setProperty(name, value);
    }
    @Override
    public Object getProperty(String name) {
        return getBaseSerializer().getProperty(name);
    }
    @Override
    public void setOutput(OutputStream os, String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().setOutput(os, encoding);
    }
    @Override
    public void setOutput(Writer writer) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().setOutput(writer);
    }
    @Override
    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().startDocument(encoding, standalone);
    }
    @Override
    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().endDocument();
    }
    @Override
    public void setPrefix(String prefix, String namespace) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().setPrefix(prefix, namespace);
    }
    @Override
    public String getPrefix(String namespace, boolean generatePrefix) throws IllegalArgumentException {
        return getBaseSerializer().getPrefix(namespace, generatePrefix);
    }
    @Override
    public int getDepth() {
        return getBaseSerializer().getDepth();
    }
    @Override
    public String getNamespace() {
        return getBaseSerializer().getNamespace();
    }
    @Override
    public String getName() {
        return getBaseSerializer().getName();
    }
    @Override
    public XmlSerializer startTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        return getBaseSerializer().startTag(namespace, name);
    }
    @Override
    public XmlSerializer attribute(String namespace, String name, String value) throws IOException, IllegalArgumentException, IllegalStateException {
        return getBaseSerializer().attribute(namespace, name, value);
    }
    @Override
    public XmlSerializer endTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        return getBaseSerializer().endTag(namespace, name);
    }
    @Override
    public XmlSerializer text(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        return getBaseSerializer().text(text);
    }
    @Override
    public XmlSerializer text(char[] buf, int start, int len) throws IOException, IllegalArgumentException, IllegalStateException {
        return getBaseSerializer().text(buf, start, len);
    }
    @Override
    public void cdsect(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().cdsect(text);
    }
    @Override
    public void entityRef(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().entityRef(text);
    }
    @Override
    public void processingInstruction(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().processingInstruction(text);
    }
    @Override
    public void comment(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().comment(text);
    }
    @Override
    public void docdecl(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().docdecl(text);
    }
    @Override
    public void ignorableWhitespace(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        getBaseSerializer().ignorableWhitespace(text);
    }
    @Override
    public void flush() throws IOException {
        getBaseSerializer().flush();
    }
}

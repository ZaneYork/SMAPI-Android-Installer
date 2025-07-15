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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class ResXmlPullSerializer implements XmlSerializer {
    private PackageBlock mCurrentPackage;
    private ResXmlDocument mDocument;
    private ResXmlElement mCurrentElement;
    private boolean mEndDocument;
    private StringBuilder mCurrentText;
    private boolean mValidateValues;
    public ResXmlPullSerializer(){

    }

    public ResXmlDocument getResultDocument() {
        ResXmlDocument document = mDocument;
        if(document != null){
            document.refreshFull();
        }
        return document;
    }
    public void setValidateValues(boolean validateValues) {
        this.mValidateValues = validateValues;
    }
    public boolean isValidateValues() {
        return mValidateValues;
    }
    public PackageBlock getCurrentPackage(){
        return mCurrentPackage;
    }
    public void setCurrentPackage(PackageBlock packageBlock){
        this.mCurrentPackage = packageBlock;
        if(mDocument != null){
            mDocument.setPackageBlock(packageBlock);
        }
    }
    public void setDocument(ResXmlDocument document) {
        this.mDocument = document;
        if(document == null){
            return;
        }
        PackageBlock packageBlock = document.getPackageBlock();
        if(packageBlock == null){
            document.setPackageBlock(getCurrentPackage());
        }else if(getCurrentPackage() == null){
            setCurrentPackage(packageBlock);
        }
    }

    private ResXmlDocument getCurrentDocument() {
        ResXmlDocument document = this.mDocument;
        if(mEndDocument){
            document = null;
            mCurrentElement = null;
            mEndDocument = false;
        }
        if(document == null){
            document = new ResXmlDocument();
            mCurrentElement = null;
            mDocument = document;
        }
        if(document.getPackageBlock() == null){
            document.setPackageBlock(getCurrentPackage());
        }
        return document;
    }

    private ResXmlElement getCurrentElement(){
        ResXmlElement element = mCurrentElement;
        if(element == null){
            ResXmlDocument document =  getCurrentDocument();
            element = document.getDocumentElement();
            if(element == null){
                element = document.createRootElement(null);
            }
            mCurrentElement = element;
        }
        return element;
    }
    @Override
    public void setFeature(String name, boolean state) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public boolean getFeature(String name) {
        return false;
    }

    @Override
    public void setProperty(String name, Object value) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public void setOutput(OutputStream os, String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new IllegalArgumentException("Can not set OutputStream");
    }

    @Override
    public void setOutput(Writer writer) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new IllegalArgumentException("Can not set OutputStream");
    }
    @Override
    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        if(mCurrentElement != null){
            mEndDocument = true;
        }
    }

    @Override
    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        mEndDocument = true;
    }

    @Override
    public void setPrefix(String prefix, String namespace) throws IOException, IllegalArgumentException, IllegalStateException {
        ResXmlElement element = getCurrentElement();
        if(element == null){
            // TODO: throw?
            return;
        }
        element.getOrCreateNamespace(namespace, prefix);
    }

    @Override
    public String getPrefix(String namespace, boolean generatePrefix) throws IllegalArgumentException {
        if(namespace == null){
            return null;
        }
        ResXmlElement element = mCurrentElement;
        if(element == null){
            // TODO: throw?
            return null;
        }
        ResXmlNamespace resXmlNamespace = element.getNamespaceByUri(namespace);
        if(resXmlNamespace == null && generatePrefix){
            String prefix;
            if(namespace.equals(ResourceLibrary.URI_ANDROID)){
                prefix = ResourceLibrary.PREFIX_ANDROID;
            }else {
                prefix = ResourceLibrary.PREFIX_APP;
            }
            resXmlNamespace = element.getOrCreateNamespace(namespace, prefix);
        }
        if(resXmlNamespace == null){
            return null;
        }
        return resXmlNamespace.getPrefix();
    }

    @Override
    public int getDepth() {
        ResXmlElement element = mCurrentElement;
        if(element != null){
            return element.getDepth();
        }
        return 0;
    }

    @Override
    public String getNamespace() {
        ResXmlElement element = mCurrentElement;
        if(element != null){
            return element.getUri();
        }
        return null;
    }
    @Override
    public String getName() {
        ResXmlElement element = mCurrentElement;
        if(element != null){
            return element.getName();
        }
        return null;
    }

    @Override
    public ResXmlPullSerializer startTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        flushText();
        ResXmlElement element = getCurrentElement();
        String prefix = null;
        int i = name.indexOf(':');
        if(i > 0){
            prefix = name.substring(0, i);
            name = name.substring(i + 1);
        }else {
            ResXmlNamespace xmlNamespace = element.getNamespaceByUri(namespace);
            if(xmlNamespace != null){
                prefix = xmlNamespace.getPrefix();
            }
        }
        if(element.getName() == null){
            element.setName(name);
        }else {
            element = element.createChildElement(name);
            mCurrentElement = element;
        }
        element.setNamespace(namespace, prefix);
        mCurrentElement = element;
        return this;
    }

    @Override
    public ResXmlPullSerializer attribute(String namespace, String name, String value) throws IOException, IllegalArgumentException, IllegalStateException {
        ResXmlElement element = mCurrentElement;
        String prefix = null;
        int i = name.indexOf(':');
        if(i > 0){
            prefix = name.substring(0, i);
            name = name.substring(i + 1);
        }
        if(prefix == null){
            ResXmlNamespace resXmlNamespace = element.getStartNamespaceByUri(namespace);
            if(resXmlNamespace != null){
                prefix = resXmlNamespace.getPrefix();
            }
        }
        ResXmlAttribute resXmlAttribute = element.newAttribute();
        resXmlAttribute.encode(isValidateValues(), namespace, prefix, name, value);
        return this;
    }

    @Override
    public ResXmlPullSerializer endTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        flushText();
        mCurrentElement.calculateAttributesOrder();
        mCurrentElement = mCurrentElement.getParentElement();
        return this;
    }

    @Override
    public ResXmlPullSerializer text(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        appendText(text);
        return this;
    }

    @Override
    public ResXmlPullSerializer text(char[] buf, int start, int len) throws IOException, IllegalArgumentException, IllegalStateException {
        return text(new String(buf, start, len));
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
        ResXmlElement current = getCurrentElement();
        current.setComment(text);
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

    private void flushText(){
        if(mCurrentText == null){
            return;
        }
        String text = mCurrentText.toString();
        mCurrentText = null;
        if(isIndent(text)){
            return;
        }
        ResXmlElement element = getCurrentElement();
        element.addResXmlText(text);
    }
    private void appendText(String text){
        if(text == null){
            return;
        }
        StringBuilder builder = mCurrentText;
        if(builder == null){
            builder = new StringBuilder();
            mCurrentText = builder;
        }
        builder.append(text);
    }
    private static boolean isIndent(String text){
        if(text.length() == 0){
            return true;
        }
        char[] chars = text.toCharArray();
        if(chars[0] != '\n'){
            return false;
        }
        for(int i = 1; i < chars.length; i++){
            if(chars[i] != ' '){
                return false;
            }
        }
        return true;
    }
}

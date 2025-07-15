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

import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.ObjectsUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * Direct implementation of:
 * https://android.googlesource.com/platform/frameworks/base/+/main/core/java/android/content/res/XmlBlock.java
 * */

public class ResXmlPullParser implements XmlResourceParser {

    private PackageBlock mCurrentPackage;
    private final ParserEventList mEventList = new ParserEventList();
    private ResXmlDocument mDocument;
    private boolean mDocumentCreatedHere;
    private DocumentLoadedListener documentLoadedListener;
    private boolean processNamespaces;
    private boolean reportNamespaceAttrs;
    private boolean mIsTagStared;

    public ResXmlPullParser(PackageBlock packageBlock){
        this.mCurrentPackage = packageBlock;
        this.processNamespaces = false;
        this.reportNamespaceAttrs = false;
    }
    public ResXmlPullParser(){
        this(null);
    }
    public PackageBlock getCurrentPackage(){
        return mCurrentPackage;
    }
    public void setCurrentPackage(PackageBlock packageBlock){
        this.mCurrentPackage = packageBlock;
        if(mDocument != null) {
            mDocument.setPackageBlock(packageBlock);
        }
    }
    public synchronized void setResXmlDocument(ResXmlDocument xmlDocument){
        closeDocument();
        this.mDocument = xmlDocument;
        PackageBlock packageBlock = xmlDocument.getPackageBlock();
        if(packageBlock == null){
            xmlDocument.setPackageBlock(getCurrentPackage());
        }
        initDefaultFeatures();
        xmlDocument.addEvents(mEventList);
    }
    public ResXmlDocument getResXmlDocument() {
        return mDocument;
    }
    public void closeDocument(){
        mEventList.clear();
        mIsTagStared = false;
        destroyDocument();
    }
    private void destroyDocument(){
        if(!mDocumentCreatedHere){
            return;
        }
        mDocumentCreatedHere = false;
        if(this.mDocument == null){
            return;
        }
        this.mDocument.destroy();
        this.mDocument = null;
    }

    @Override
    public void close(){
        closeDocument();
    }
    @Override
    public int getAttributeCount() {
        ResXmlElement element = getCurrentElement();
        if(element == null){
            return 0;
        }
        int count = element.getAttributeCount();
        if(isCountNamespacesAsAttribute()){
            count += element.getNamespaceCount();
        }
        return count;
    }
    @Override
    public String getAttributeName(int index) {
        if(isCountNamespacesAsAttribute()){
            int nsCount = getNamespaceCountInternal();
            if(index < nsCount){
                return getNamespaceAttributeName(index);
            }
        }
        return decodeAttributeName(getResXmlAttributeAt(index));
    }
    @Override
    public String getAttributeValue(int index) {
        if(isCountNamespacesAsAttribute()){
            int nsCount = getNamespaceCountInternal();
            if(index < nsCount){
                return getNamespaceAttributeValue(index);
            }
        }
        return decodeAttributeValue(getResXmlAttributeAt(index));
    }
    @Override
    public String getAttributeValue(String namespace, String name) {
        return decodeAttributeValue(getAttribute(namespace, name));
    }
    @Override
    public String getPositionDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(" Binary XML file line #");
        builder.append(mEventList.getLineNumber());
        ResXmlElement element = getCurrentElement();
        if(element != null) {
            if (mEventList.getType() == START_TAG) {
                builder.append(" START_TAG ");
            } else {
                builder.append(" END_TAG ");
            }
            builder.append('<');
            builder.append(element.getName(true));
            builder.append('>');
        }
        return builder.toString();
    }
    @Override
    public int getAttributeNameResource(int index) {
        ResXmlAttribute attribute = getResXmlAttributeAt(index);
        if(attribute!=null){
            return attribute.getNameId();
        }
        return 0;
    }
    @Override
    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute != null) {
            int v = getAttributeIntValue(xmlAttribute, 0);
            return v != 0;
        }
        return defaultValue;
    }
    @Override
    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute != null && xmlAttribute.getValueType() == ValueType.REFERENCE){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        return getAttributeIntValue(getAttribute(namespace, attribute), defaultValue);
    }
    @Override
    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        return getAttributeIntValue(getAttribute(namespace, attribute), defaultValue);
    }
    @Override
    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute != null){
            ValueType valueType = xmlAttribute.getValueType();
            if(valueType == ValueType.FLOAT) {
                return Float.intBitsToFloat(xmlAttribute.getData());
            }
        }
        return defaultValue;
    }

    @Override
    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute == null ||
                xmlAttribute.getValueType() != ValueType.STRING
                || options == null || options.length == 0){
            return defaultValue;
        }
        return convertValueToList(xmlAttribute.getValueAsString(), options, defaultValue);
    }
    @Override
    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        ResXmlAttribute xmlAttribute = getAttribute(namespace, attribute);
        if(xmlAttribute == null ||
                xmlAttribute.getValueType() != ValueType.STRING
                || options == null || options.length == 0){
            return defaultValue;
        }
        return convertValueToList(xmlAttribute.getValueAsString(), options, defaultValue);
    }
    @Override
    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute != null) {
            int v = getAttributeIntValue(xmlAttribute, 0);
            return v != 0;
        }
        return defaultValue;
    }
    @Override
    public int getAttributeResourceValue(int index, int defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute != null && xmlAttribute.getValueType() == ValueType.REFERENCE){
            return xmlAttribute.getData();
        }
        return defaultValue;
    }
    @Override
    public int getAttributeIntValue(int index, int defaultValue) {
        return getAttributeIntValue(getResXmlAttributeAt(index), defaultValue);
    }
    @Override
    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        return getAttributeIntValue(getResXmlAttributeAt(index), defaultValue);
    }
    @Override
    public float getAttributeFloatValue(int index, float defaultValue) {
        ResXmlAttribute xmlAttribute = getResXmlAttributeAt(index);
        if(xmlAttribute != null) {
            if(xmlAttribute.getValueType() == ValueType.FLOAT){
                return Float.intBitsToFloat(xmlAttribute.getData());
            }
        }
        return defaultValue;
    }

    @Override
    public String getIdAttribute() {
        ResXmlElement currentElement = getCurrentElement();
        if(currentElement != null){
            ResXmlAttribute attribute = currentElement.getIdAttribute();
            if(attribute != null){
                return attribute.getName();
            }
        }
        return null;
    }
    @Override
    public String getClassAttribute() {
        ResXmlElement currentElement = getCurrentElement();
        if(currentElement!=null){
            ResXmlAttribute attribute = currentElement.getClassAttribute();
            if(attribute != null){
                return attribute.getName();
            }
        }
        return null;
    }
    @Override
    public int getIdAttributeResourceValue(int defaultValue) {
        ResXmlElement currentElement = getCurrentElement();
        if(currentElement != null){
            ResXmlAttribute attribute = currentElement.getIdAttribute();
            if(attribute != null){
                return attribute.getNameId();
            }
        }
        return defaultValue;
    }
    @Override
    public int getStyleAttribute() {
        ResXmlElement currentElement = getCurrentElement();
        if(currentElement != null){
            ResXmlAttribute attribute = currentElement.getStyleAttribute();
            if(attribute != null){
                return attribute.getNameId();
            }
        }
        return 0;
    }

    @Override
    public void setFeature(String name, boolean state) throws XmlPullParserException {
        boolean changed;
        if(FEATURE_PROCESS_NAMESPACES.equals(name)) {
            changed = processNamespaces != state;
            processNamespaces = state;
        }else if(FEATURE_REPORT_NAMESPACE_ATTRIBUTES.equals(name)) {
            changed = reportNamespaceAttrs != state;
            reportNamespaceAttrs = state;
        }else {
            throw new XmlPullParserException("Unsupported feature: " + name);
        }
        if(changed && mIsTagStared){
            throw new XmlPullParserException("Feature changed during parsing: "
                    + name + ", state=" + state);
        }
    }

    @Override
    public boolean getFeature(String name) {
        if(FEATURE_PROCESS_NAMESPACES.equals(name)) {
            return processNamespaces;
        }else if(FEATURE_REPORT_NAMESPACE_ATTRIBUTES.equals(name)) {
            return reportNamespaceAttrs;
        }
        return false;
    }
    @Override
    public void setProperty(String name, Object value) throws XmlPullParserException {
    }
    @Override
    public Object getProperty(String name) {
        return null;
    }
    @Override
    public void setInput(Reader in) throws XmlPullParserException {
        InputStream inputStream = getFromLock(in);
        if(inputStream == null){
            throw new XmlPullParserException("Can't parse binary xml from reader");
        }
        setInput(inputStream, null);
    }
    @Override
    public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
        loadResXmlDocument(inputStream);
    }
    @Override
    public String getInputEncoding() {
        // Not applicable but let not return null
        return "UTF-8";
    }
    @Override
    public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
    }
    @Override
    public int getNamespaceCount(int depth) throws XmlPullParserException {
        if(isCountNamespacesAsAttribute()){
            return 0;
        }
        ResXmlElement element = getCurrentElement();
        while(element!=null && element.getDepth()>depth){
            element=element.getParentElement();
        }
        if(element!=null){
            return element.getNamespaceCount();
        }
        return 0;
    }
    @Override
    public String getNamespacePrefix(int pos) throws XmlPullParserException {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getNamespaceAt(pos).getPrefix();
        }
        return null;
    }
    @Override
    public String getNamespaceUri(int pos) throws XmlPullParserException {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getNamespaceAt(pos).getUri();
        }
        return null;
    }
    @Override
    public String getNamespace(String prefix) {
        ResXmlElement element = getCurrentElement();
        if(element != null){
            ResXmlNamespace namespace = element.getNamespaceByPrefix(prefix);
            if(namespace != null){
                return namespace.getUri();
            }
        }
        return null;
    }
    @Override
    public int getDepth() {
        int event = mEventList.getType();
        if(event == START_TAG || event == END_TAG || event == TEXT){
            return mEventList.getXmlNode().getDepth();
        }
        return 0;
    }
    @Override
    public int getLineNumber() {
        return mEventList.getLineNumber();
    }
    @Override
    public int getColumnNumber() {
        return 0;
    }
    @Override
    public boolean isWhitespace() throws XmlPullParserException {
        String text = getText();
        if(text == null){
            return true;
        }
        text = text.trim();
        return text.length() == 0;
    }
    @Override
    public String getText() {
        return mEventList.getText();
    }
    @Override
    public char[] getTextCharacters(int[] holderForStartAndLength) {
        String text = getText();
        if (text == null) {
            holderForStartAndLength[0] = -1;
            holderForStartAndLength[1] = -1;
            return null;
        }
        char[] result = text.toCharArray();
        holderForStartAndLength[0] = 0;
        holderForStartAndLength[1] = result.length;
        return result;
    }
    @Override
    public String getNamespace() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getUri();
        }
        return null;
    }
    @Override
    public String getName() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getName();
        }
        return null;
    }
    @Override
    public String getPrefix() {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.getPrefix();
        }
        return null;
    }
    @Override
    public boolean isEmptyElementTag() throws XmlPullParserException {
        ResXmlElement element = getCurrentElement();
        if(element!=null){
            return element.size() == 0 && element.getAttributeCount()==0;
        }
        return true;
    }
    @Override
    public String getAttributeNamespace(int index) {
        if(processNamespaces){
            return null;
        }
        ResXmlAttribute attribute = getResXmlAttributeAt(index);
        if(attribute != null){
            return attribute.getUri();
        }
        return null;
    }
    @Override
    public String getAttributePrefix(int index) {
        if(processNamespaces){
            return null;
        }
        ResXmlAttribute attribute = getResXmlAttributeAt(index);
        if(attribute != null){
            return attribute.getPrefix();
        }
        return null;
    }
    @Override
    public String getAttributeType(int index) {
        return "CDATA";
    }
    @Override
    public boolean isAttributeDefault(int index) {
        return false;
    }
    private String decodeAttributeName(ResXmlAttribute attribute){
        if(attribute != null){
            return attribute.decodeName(processNamespaces);
        }
        return null;
    }
    private String decodeAttributeValue(ResXmlAttribute attribute){
        if(attribute != null){
            String value = attribute.decodeValue();
            if(attribute.getValueType() == ValueType.STRING){
                value = XmlSanitizer.escapeSpecialCharacter(value);
            }
            return value;
        }
        return null;
    }
    public ResXmlAttribute getResXmlAttributeAt(int index){
        index = getRealAttributeIndex(index);
        ResXmlElement element = getCurrentElement();
        if(element == null){
            return null;
        }
        return element.getAttributeAt(index);
    }
    public ResXmlAttribute getAttribute(String namespace, String name) {
        ResXmlElement element = getCurrentElement();
        if(element == null){
            return null;
        }
        Iterator<ResXmlAttribute> iterator = element.getAttributes();
        while (iterator.hasNext()){
            ResXmlAttribute attribute = iterator.next();
            if(ObjectsUtil.equals(namespace, attribute.getUri())
                    && ObjectsUtil.equals(name, attribute.getName())){
                return attribute;
            }
        }
        return null;
    }
    public ResXmlElement getCurrentElement() {
        int type = mEventList.getType();
        if(type == START_TAG || type == END_TAG){
            return mEventList.getElement();
        }
        return null;
    }
    private int convertValueToList(String value, String[] options, int defaultValue) {
        if (!TextUtils.isEmpty(value)) {
            for (int i = 0; i < options.length; i++) {
                if (value.equals(options[i])) {
                    return i;
                }
            }
        }
        return defaultValue;
    }

    private int getAttributeIntValue(ResXmlAttribute xmlAttribute, int defaultValue) {
        if(xmlAttribute != null) {
            int type = xmlAttribute.getType() & 0xff;
            if(type > 0x10 && type <= 0x1f) {
                return xmlAttribute.getData();
            }
            // TODO: resolve if type is REFERENCE
        }
        return defaultValue;
    }
    private int getRealAttributeIndex(int index){
        if(isCountNamespacesAsAttribute()){
            index = index - getNamespaceCountInternal();
        }
        return index;
    }
    private int getNamespaceCountInternal(){
        ResXmlElement element = getCurrentElement();
        if(element != null){
            return element.getNamespaceCount();
        }
        return 0;
    }
    private boolean isCountNamespacesAsAttribute(){
        return processNamespaces & reportNamespaceAttrs;
    }
    private String getNamespaceAttributeName(int index){
        ResXmlNamespace namespace = getCurrentElement()
                .getNamespaceAt(index);
        return "xmlns:" + namespace.getPrefix();
    }
    private String getNamespaceAttributeValue(int index){
        ResXmlNamespace namespace = getCurrentElement()
                .getNamespaceAt(index);
        return namespace.getUri();
    }
    @Override
    public int getEventType() throws XmlPullParserException {
        return mEventList.getType();
    }
    @Override
    public int next() throws XmlPullParserException, IOException {
        mEventList.next();
        int type = mEventList.getType();
        if(type == START_TAG){
            mIsTagStared = true;
        }
        return type;
    }
    @Override
    public int nextToken() throws XmlPullParserException, IOException {
        return next();
    }
    @Override
    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
        if (type != this.getEventType()
                || (namespace != null && !namespace.equals(getNamespace()))
                || (name != null && !name.equals(getName()))) {
            throw new XmlPullParserException(
                    "expected: " + TYPES[type] + " {" + namespace + "}" + name, this, null);
        }
    }
    @Override
    public String nextText() throws XmlPullParserException, IOException {
        int event = getEventType();
        if (event != START_TAG) {
            throw new XmlPullParserException("precondition: START_TAG", this, null);
        }
        while (event!=TEXT && event!=END_TAG && event!=END_DOCUMENT){
            event=next();
        }
        if(event==TEXT){
            return getText();
        }
        return "";
    }
    @Override
    public int nextTag() throws XmlPullParserException, IOException {
        int event = getEventType();
        if (event != START_TAG) {
            throw new XmlPullParserException("precondition: START_TAG", this, null);
        }
        event = next();
        while (event!=START_TAG && event!=END_DOCUMENT){
            event=next();
        }
        return event;
    }

    private static InputStream getFromLock(Reader reader){
        try{
            Field field = Reader.class.getDeclaredField("lock");
            field.setAccessible(true);
            Object obj = field.get(reader);
            if(obj instanceof InputStream){
                return (InputStream) obj;
            }
        }catch (Throwable ignored){
        }
        return null;
    }

    public void setDocumentLoadedListener(DocumentLoadedListener documentLoadedListener) {
        this.documentLoadedListener = documentLoadedListener;
    }

    private void loadResXmlDocument(InputStream inputStream) throws XmlPullParserException {
        synchronized (this){
            ResXmlDocument xmlDocument = new ResXmlDocument();
            try {
                xmlDocument.readBytes(inputStream);
                xmlDocument.setPackageBlock(getCurrentPackage());
            } catch (IOException exception) {
                XmlPullParserException pullParserException = new XmlPullParserException(exception.getMessage());
                pullParserException.initCause(exception);
                throw pullParserException;
            }
            DocumentLoadedListener listener = this.documentLoadedListener;
            if(listener != null){
                xmlDocument = listener.onDocumentLoaded(xmlDocument);
            }
            setResXmlDocument(xmlDocument);
            this.mDocumentCreatedHere = true;
        }
    }
    private void initDefaultFeatures(){
        processNamespaces = true;
        reportNamespaceAttrs = true;
    }

    public static interface DocumentLoadedListener{
        public ResXmlDocument onDocumentLoaded(ResXmlDocument resXmlDocument);
    }

}

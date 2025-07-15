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
package com.reandroid.arsc.item;

import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.coder.ThreeByteCharsetDecoder;
import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.xml.StyleDocument;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class StringItem extends StringBlock implements JSONConvert<JSONObject>, Comparable<StringItem> {

    private boolean mUtf8;
    private final Set<ReferenceItem> mReferencedList;
    private StyleItem mStyleItem;

    public StringItem(boolean utf8) {
        super();
        this.mUtf8 = utf8;
        this.mReferencedList = new HashSet<>();
    }

    public StyleDocument getStyleDocument() {
        if(hasStyle()){
            return getStyle().build(get());
        }
        return null;
    }

    public<T extends Block> Iterator<T> getUsers(Class<T> parentClass){
        return getUsers(parentClass, null);
    }
    public<T extends Block> Iterator<T> getUsers(Class<T> parentClass,
                                                 Predicate<T> resultFilter){

        Collection<ReferenceItem> referencedList = getReferencedList();
        if(referencedList.size() == 0){
            return EmptyIterator.of();
        }
        return new ComputeIterator<>(referencedList.iterator(), referenceItem -> {
            T result = referenceItem.getReferredParent(parentClass);
            if (result == null || resultFilter != null && !resultFilter.test(result)) {
                result = null;
            }
            return result;
        });

    }

    public boolean removeReference(ReferenceItem ref){
        return mReferencedList.remove(ref);
    }
    public void removeAllReference(){
        mReferencedList.clear();
    }
    public boolean hasReference(){
        ensureStringLinkUnlocked();
        if(mReferencedList.size() == 0) {
            return false;
        }
        return FilterIterator.of(mReferencedList.iterator(),
                referenceItem -> !(referenceItem instanceof StyleItem.StyleIndexReference))
                .hasNext();
    }
    public Collection<ReferenceItem> getReferencedList(){
        ensureStringLinkUnlocked();
        return mReferencedList;
    }
    void ensureStringLinkUnlocked(){
        StringPool<?> stringPool = getParentInstance(StringPool.class);
        if(stringPool != null){
            stringPool.ensureStringLinkUnlockedInternal();
        }
    }
    public void addReference(ReferenceItem ref){
        if(ref!=null){
            mReferencedList.add(ref);
        }
    }
    public void addReferenceIfAbsent(ReferenceItem ref){
        if(ref!=null){
            mReferencedList.add(ref);
        }
    }
    public void addReference(Collection<ReferenceItem> refList){
        if(refList == null){
            return;
        }
        for(ReferenceItem ref:refList){
            if(ref != null){
                this.mReferencedList.add(ref);
            }
        }
    }
    private void reUpdateReferences(int newIndex){
        ReferenceItem[] referenceItems = mReferencedList.toArray(new ReferenceItem[0]);
        for(ReferenceItem ref:referenceItems){
            ref.set(newIndex);
        }
    }
    public void onRemoved(){
        clearStyle();
        setParent(null);
    }
    @Override
    public void onIndexChanged(int oldIndex, int newIndex){
        reUpdateReferences(newIndex);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected void onStringChanged(String old, String text) {
        super.onStringChanged(old, text);
        StringPool<StringItem> stringPool = getParentInstance(StringPool.class);
        if(stringPool != null) {
            stringPool.onStringChanged(old, this);
        }
    }

    public void serializeText(XmlSerializer serializer) throws IOException {
        serializeText(serializer, false);
    }
    public void serializeText(XmlSerializer serializer, boolean escapeValues) throws IOException {
        String text = get();
        if(text == null){
            return;
        }
        if(escapeValues){
            text = XmlSanitizer.escapeDecodedValue(text);
        }else {
            text = XmlSanitizer.escapeSpecialCharacter(text);
        }
        serializer.text(text);
    }
    public void serializeAttribute(XmlSerializer serializer, String namespace, String name) throws IOException {
        String text = get();
        if(text == null){
            // TODO: could happen?
            text = "";
        }
        serializer.attribute(namespace, name, XmlSanitizer.escapeSpecialCharacter(text));
    }
    public String getHtml(){
        String text = get();
        if(text == null){
            return null;
        }
        StyleItem styleItem = getStyle();
        if(styleItem == null){
            return text;
        }
        return styleItem.applyStyle(text, false, false);
    }
    public String getXml(){
        return getXml(false);
    }
    public String getXml(boolean escapeXmlText){
        String text = get();
        if(text == null){
            return null;
        }
        StyleItem styleItem = getStyle();
        if(styleItem == null){
            return text;
        }
        return styleItem.applyStyle(text, true, escapeXmlText);
    }
    @Override
    public void set(String str){
        if(str == null){
            StyleItem style = getStyle();
            if(style != null){
                style.clearStyle();
            }
        }
        super.set(str);
    }
    public void set(StyleDocument document){
        String old = getXml();
        if(countBytes() == 0) {
            old = null;
        }
        clearStyle();
        this.set(document.getStyledString(), false);
        if(document.hasElements()) {
            StyleItem styleItem = getOrCreateStyle();
            styleItem.parse(document);
        }
        String update = getXml();
        onStringChanged(old, update);
    }
    public void set(JSONObject jsonObject){
        String old = getXml();
        if(countBytes() == 0) {
            old = null;
        }
        clearStyle();
        this.set(jsonObject.getString(NAME_string), false);
        JSONObject style = jsonObject.optJSONObject(NAME_style);
        if(style != null) {
            StyleItem styleItem = getOrCreateStyle();
            styleItem.fromJson(style);
        }
        String update = getXml();
        onStringChanged(old, update);
    }

    public boolean isUtf8(){
        return mUtf8;
    }
    public void setUtf8(boolean utf8){
        if(utf8 == mUtf8){
            return;
        }
        mUtf8 = utf8;
        onBytesChanged();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        if(reader.available() < 4){
            return;
        }
        setBytesLength(calculateReadLength(reader), false);
        reader.readFully(getBytesInternal());
        onBytesChanged();
    }
    int calculateReadLength(BlockReader reader) throws IOException {
        if(reader.available() < 4){
            return reader.available();
        }
        byte[] bytes = new byte[4];
        reader.readFully(bytes);
        reader.offset(-4);
        int[] lengthResult;
        if(isUtf8()){
            lengthResult = decodeUtf8StringByteLength(bytes);
        }else {
            lengthResult = decodeUtf16StringByteLength(bytes);
        }
        int add = isUtf8()? 1:2;
        return lengthResult[0] + lengthResult[1] + add;
    }
    @Override
    protected String decodeString(byte[] bytes){
        return decodeString(bytes, mUtf8);
    }
    @Override
    protected byte[] encodeString(String str){
        if(mUtf8){
            return encodeUtf8ToBytes(str);
        }else {
            return encodeUtf16ToBytes(str);
        }
    }
    private String decodeString(byte[] allStringBytes, boolean isUtf8) {
        if(isNullBytes(allStringBytes)){
            if(allStringBytes==null||allStringBytes.length==0){
                return null;
            }
            return "";
        }
        int[] offLen;
        if(isUtf8){
            offLen=decodeUtf8StringByteLength(allStringBytes);
        }else {
            offLen=decodeUtf16StringByteLength(allStringBytes);
        }
        CharsetDecoder charsetDecoder;
        if(isUtf8){
            charsetDecoder=UTF8_DECODER;
        }else {
            charsetDecoder=UTF16LE_DECODER;
        }
        try {
            ByteBuffer buf=ByteBuffer.wrap(allStringBytes, offLen[0], offLen[1]);
            CharBuffer charBuffer=charsetDecoder.decode(buf);
            return charBuffer.toString();
        } catch (CharacterCodingException ex) {
            if(isUtf8){
                return tryThreeByteDecoder(allStringBytes, offLen[0], offLen[1]);
            }
            return new String(allStringBytes, offLen[0], offLen[1], StandardCharsets.UTF_16LE);
        }
    }
    private String tryThreeByteDecoder(byte[] bytes, int offset, int length){
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, offset, length);
            CharBuffer charBuffer = DECODER_3B.decode(byteBuffer);
            return charBuffer.toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, offset, length, StandardCharsets.UTF_8);
        }
    }
    public boolean hasStyle(){
        StyleItem styleItem=getStyle();
        if(styleItem==null){
            return false;
        }
        return styleItem.size()>0;
    }
    public StyleItem getStyle(){
        return mStyleItem;
    }
    public StyleItem getOrCreateStyle(){
        StyleItem styleItem = getStyle();
        if(styleItem == null) {
            styleItem = getParentInstance(StringPool.class).getStyleArray().createNext();
            linkStyleItemInternal(styleItem);
            styleItem = getStyle();
        }
        return styleItem;
    }
    public void linkStyleItemInternal(StyleItem styleItem) {
        if(styleItem == null) {
            throw new NullPointerException("Can not link null style item");
        }
        if(this.mStyleItem == styleItem) {
            return;
        }
        if(this.mStyleItem != null) {
            throw new IllegalStateException("Style item is already linked");
        }
        this.mStyleItem = styleItem;
        styleItem.setStringItemInternal(this);
    }
    public void unlinkStyleItemInternal(StyleItem styleItem) {
        if(this.mStyleItem == null) {
            return;
        }
        if(styleItem != this.mStyleItem) {
            throw new IllegalStateException("Wrong style item");
        }
        this.mStyleItem = null;
        styleItem.setStringItemInternal(null);
    }
    private void clearStyle() {
        StyleItem styleItem = getStyle();
        if(styleItem != null) {
            styleItem.clearStyle();
        }
    }
    public void transferReferences(StringItem source){
        if(source == this || source == null || getParent() != source.getParent()){
            return;
        }
        int index = getIndex();
        if(index < 0 || source.getIndex() < 0){
            return;
        }
        ReferenceItem[] copyList = source.getReferencedList().toArray(new ReferenceItem[0]);
        for(ReferenceItem ref : copyList){
            if(isTransferable(ref)){
                source.removeReference(ref);
                ref.set(index);
                addReference(ref);
            }
        }
    }
    private boolean isTransferable(ReferenceItem referenceItem){
        return !((referenceItem instanceof WeakStringReference));
    }
    public boolean merge(StringItem other) {
        if(!canMerge(other)) {
            return false;
        }
        clearStyle();
        set(other.get(), false);
        StyleItem otherStyle = other.getStyle();
        if(otherStyle != null && otherStyle.hasSpans()) {
            getOrCreateStyle().merge(otherStyle);
        }
        onStringChanged(null, getXml());
        return true;
    }
    boolean canMerge(StringItem stringItem) {
        if(stringItem == null || stringItem == this) {
            return false;
        }
        Block array1 = this.getParentInstance(StringArray.class);
        Block array2 = stringItem.getParentInstance(StringArray.class);
        return array1 != null && array2 != null && array1 != array2;
    }
    @Override
    public int compareTo(StringItem stringItem) {
        if(stringItem == null){
            return -1;
        }
        if(stringItem == this) {
            return 0;
        }
        int i = -1 * CompareUtil.compare(hasStyle(), stringItem.hasStyle());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getXml(), stringItem.getXml());
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_string, get());
        if(hasStyle()) {
            jsonObject.put(NAME_style, getStyle().toJson());
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        set(json);
    }
    @Override
    public String toString(){
        String xml = getXml();
        if(xml == null){
            return getIndex() + ": NULL";
        }
        StringPool<?> stringPool = getParentInstance(StringPool.class);
        if(stringPool != null && !stringPool.isStringLinkLocked()){
            return getIndex() + ": USED BY=" + mReferencedList.size() + "{" + xml + "}";
        }
        return getIndex() + ":" + xml;
    }

    private static int[] decodeUtf8StringByteLength(byte[] lengthBytes) {
        int offset=0;
        int val = lengthBytes[offset];
        int length;
        if ((val & 0x80) != 0) {
            offset += 2;
        } else {
            offset += 1;
        }
        val = lengthBytes[offset];
        offset += 1;
        if ((val & 0x80) != 0) {
            int low = (lengthBytes[offset] & 0xFF);
            length = val & 0x7F;
            length = length << 8;
            length = length + low;
            offset += 1;
        } else {
            length = val;
        }
        return new int[] { offset, length};
    }
    private static int[] decodeUtf16StringByteLength(byte[] lengthBytes) {
        int val = ((lengthBytes[1] & 0xFF) << 8 | lengthBytes[0] & 0xFF);
        if ((val & 0x8000) != 0) {
            int high = (lengthBytes[3] & 0xFF) << 8;
            int low = (lengthBytes[2] & 0xFF);
            int len_value =  ((val & 0x7FFF) << 16) + (high + low);
            return new int[] {4, len_value * 2};

        }
        return new int[] {2, val * 2};
    }
    static boolean isNullBytes(byte[] bts){
        if(bts==null){
            return true;
        }
        int max=bts.length;
        if(max<2){
            return true;
        }
        for(int i=2; i<max;i++){
            if(bts[i] != 0){
                return false;
            }
        }
        return true;
    }


    private static byte[] encodeUtf8ToBytes(String str){
        byte[] bts;
        byte[] lenBytes=new byte[2];
        if(str!=null){
            bts=str.getBytes();
            int strLen=bts.length;
            if((strLen & 0xff80)!=0){
                lenBytes=new byte[4];
                int l2=strLen&0xff;
                int l1=(strLen-l2)>>8;
                lenBytes[3]=(byte) (l2);
                lenBytes[2]=(byte) (l1|0x80);
                strLen=str.length();
                l2=strLen&0xff;
                l1=(strLen-l2)>>8;
                lenBytes[1]=(byte) (l2);
                lenBytes[0]=(byte) (l1|0x80);
            }else{
                lenBytes=new ShortItem((short) strLen).getBytesInternal();
                lenBytes[1]=lenBytes[0];
                lenBytes[0]=(byte)str.length();
            }
        }else {
            bts=new byte[0];
        }
        return addBytes(lenBytes, bts, new byte[1]);
    }
    private static byte[] encodeUtf16ToBytes(String str){
        if(str==null){
            return null;
        }
        byte[] lenBytes;
        byte[] bts=getUtf16Bytes(str);
        int strLen=bts.length;
        strLen=strLen/2;
        if((strLen & 0xffff8000)!=0){
            lenBytes=new byte[4];
            int low=strLen&0xff;
            int high=(strLen-low)&0xff00;
            int rem=strLen-low-high;
            lenBytes[3]=(byte) (high>>8);
            lenBytes[2]=(byte) (low);
            low=rem&0xff;
            high=(rem&0xff00)>>8;
            lenBytes[1]=(byte) (high|0x80);
            lenBytes[0]=(byte) (low);
        }else{
            lenBytes=new ShortItem((short) strLen).getBytesInternal();
        }
        return addBytes(lenBytes, bts, new byte[2]);
    }
    static byte[] getUtf16Bytes(String str){
        try {
            return str.getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] addBytes(byte[] bts1, byte[] bts2, byte[] bts3){
        if(bts1==null && bts2==null && bts3==null){
            return null;
        }
        int len=0;
        if(bts1!=null){
            len=bts1.length;
        }
        if(bts2!=null){
            len+=bts2.length;
        }
        if(bts3!=null){
            len+=bts3.length;
        }
        byte[] result=new byte[len];
        int start=0;
        if(bts1!=null){
            start=bts1.length;
            System.arraycopy(bts1, 0, result, 0, start);
        }
        if(bts2!=null){
            System.arraycopy(bts2, 0, result, start, bts2.length);
            start+=bts2.length;
        }
        if(bts3!=null){
            System.arraycopy(bts3, 0, result, start, bts3.length);
        }
        return result;
    }

    private static final CharsetDecoder UTF16LE_DECODER = StandardCharsets.UTF_16LE.newDecoder();
    private static final CharsetDecoder DECODER_3B = ThreeByteCharsetDecoder.INSTANCE;

    public static final String NAME_string = ObjectsUtil.of("string");
    public static final String NAME_style = ObjectsUtil.of("style");
}

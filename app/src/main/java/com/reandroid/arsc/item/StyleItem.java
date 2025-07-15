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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.SpanSet;
import com.reandroid.xml.StyleDocument;
import com.reandroid.xml.StyleElement;
import com.reandroid.xml.StyleSpanEventSet;

import java.io.IOException;
import java.util.Iterator;

public class StyleItem extends FixedBlockContainer implements
        Comparable<StyleItem>, Iterable<StyleSpan>, SpanSet<StyleSpan>, JSONConvert<JSONObject> {

    private final BlockList<StyleSpan> spanList;
    private final IntegerItem endBlock;

    private StyleIndexReference indexReference;
    private StringItem mStringItem;

    public StyleItem() {
        super(2);
        this.spanList = new BlockList<>();
        this.endBlock = new IntegerItem();
        addChild(0, spanList);
        addChild(1, endBlock);
        endBlock.set(-1);
    }
    public void add(String tag, int start, int end){
        StyleSpan styleSpan = createNext();
        styleSpan.setString(tag);
        styleSpan.setFirstChar(start);
        styleSpan.setLastChar(end);
    }
    public StyleSpan createNext(){
        StyleSpan styleSpan = new StyleSpan();
        this.spanList.add(styleSpan);
        return styleSpan;
    }
    public StyleSpan get(int i){
        return spanList.get(i);
    }
    public int size(){
        return spanList.size();
    }
    public boolean hasSpans() {
        return spanList.size() != 0;
    }
    @Override
    public Iterator<StyleSpan> iterator() {
        return spanList.clonedIterator();
    }
    @Override
    public Iterator<StyleSpan> getSpans() {
        return iterator();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        BlockList<StyleSpan> spanList = this.spanList;
        while (reader.readInteger() != -1){
            StyleSpan styleSpan = new StyleSpan();
            spanList.add(styleSpan);
            styleSpan.onReadBytes(reader);
        }
        this.endBlock.onReadBytes(reader);
    }

    public StyleDocument build(String text){
        return StyleSpanEventSet.serialize(text, this);
    }
    public void parse(StyleDocument document){
        clearSpans();
        Iterator<StyleElement> iterator = document.getElements();
        while (iterator.hasNext()){
            parse(iterator.next());
        }
    }
    public void parse(StyleElement element){
        add(element.getTagString(), element.getFirstChar(), element.getLastChar());
        Iterator<StyleElement> iterator = element.getElements();
        while (iterator.hasNext()){
            parse(iterator.next());
        }
    }
    protected void clearStyle(){
        StringItem stringItem = getStringItemInternal();
        if(stringItem != null) {
            stringItem.unlinkStyleItemInternal(this);
        }
        clearSpans();
    }
    protected void clearSpans(){
        if(getParent() == null){
            return;
        }
        for(StyleSpan styleSpan : this){
            styleSpan.onRemoved();
        }
        spanList.clearChildes();
    }
    public void onRemoved(){
        clearStyle();
    }
    public void linkStringsInternal(){
        for(StyleSpan styleSpan : this){
            styleSpan.link();
        }
    }
    public void setStringItemInternal(StringItem stringItem) {
        if(stringItem == null) {
            StringItem exist = this.mStringItem;
            this.mStringItem = null;
            unLinkIndexReference(exist);
            return;
        }
        if(this.mStringItem != null) {
            if(stringItem == this.mStringItem) {
                return;
            }
            throw new IllegalStateException("Different string item");
        }
        this.mStringItem = stringItem;
        StyleIndexReference reference = new StyleIndexReference(this);
        stringItem.addReference(reference);
        this.indexReference = reference;
    }
    public StringItem getStringItemInternal() {
        return mStringItem;
    }
    public boolean isEmpty() {
        StringItem stringItem = getStringItemInternal();
        if(stringItem == null) {
            return true;
        }
        return !hasSpans();
    }

    private void unLinkIndexReference(StringItem stringItem){
        StyleIndexReference reference = this.indexReference;
        if(reference == null){
            return;
        }
        this.indexReference = null;
        if(stringItem == null){
            return;
        }
        stringItem.removeReference(reference);
    }

    public String applyStyle(String text, boolean xml, boolean escapeXmlText){
        if(text == null){
            return null;
        }
        StyleDocument styleDocument = build(text);
        if(styleDocument == null){
            return text;
        }
        return styleDocument.getText(xml, escapeXmlText);
    }
    @Override
    public void setNull(boolean is_null){
        if(!is_null){
            return;
        }
        clearStyle();
    }
    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        JSONObject jsonObject=new JSONObject();
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(StyleSpan spanInfo:this){
            if(spanInfo==null){
                continue;
            }
            JSONObject jsonObjectSpan=spanInfo.toJson();
            jsonArray.put(i, jsonObjectSpan);
            i++;
        }
        if(i==0){
            return null;
        }
        jsonObject.put(NAME_spans, jsonArray);
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        clearSpans();
        if(json == null){
            clearStyle();
            return;
        }
        JSONArray jsonArray = json.getJSONArray(NAME_spans);
        int length = jsonArray.length();
        for(int i = 0; i < length; i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            StyleSpan styleSpan = createNext();
            styleSpan.fromJson(jsonObject);
        }
    }
    public void merge(StyleItem styleItem){
        if(styleItem == null || styleItem == this){
            return;
        }
        for(StyleSpan styleSpan : styleItem){
            add(styleSpan.getString(), styleSpan.getFirstChar(), styleSpan.getLastChar());
        }
    }

    @Override
    public int compareTo(StyleItem styleItem) {
        if(styleItem == this) {
            return 0;
        }
        if(styleItem == null) {
            return -1;
        }
        int i = -1 * CompareUtil.compare(this.hasSpans(), styleItem.hasSpans());
        if(i != 0) {
            return i;
        }
        StringItem stringItem1 = this.getStringItemInternal();
        StringItem stringItem2 = styleItem.getStringItemInternal();
        i = CompareUtil.compare(stringItem1 == null, stringItem2 == null);
        if(i != 0 || stringItem1 == null || stringItem2 == null) {
            return i;
        }
        return CompareUtil.compareUnsigned(stringItem1.getIndex(), stringItem2.getIndex());
    }

    @Override
    public String toString(){
        return "Spans count = " + size();
    }

    static final class StyleIndexReference implements WeakStringReference{
        private final StyleItem styleItem;
        private int index;
        StyleIndexReference(StyleItem styleItem){
            this.styleItem = styleItem;
            this.index = styleItem.getIndex();
        }
        @Override
        public void set(int value) {
            this.index = value;
        }
        @Override
        public int get() {
            return index;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass) {
            if(parentClass.isInstance(styleItem)){
                return (T1) styleItem;
            }
            return null;
        }
    }

    public static final String NAME_spans = ObjectsUtil.of("spans");
}

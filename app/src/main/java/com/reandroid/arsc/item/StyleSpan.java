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

import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.Span;

public class StyleSpan extends BlockItem implements Span, JSONConvert<JSONObject> {

    private final SpanStringReference stringReference;
    private final IndirectInteger firstChar;
    private final IndirectInteger lastChar;

    public StyleSpan() {
        super(12);
        this.stringReference = new SpanStringReference(this);
        this.firstChar = new IndirectInteger(this, 4);
        this.lastChar = new IndirectInteger(this, 8);
    }

    public String getString(){
        return stringReference.getString();
    }
    public void setString(String value) {
        stringReference.setString(value);
    }
    @Override
    public int getFirstChar() {
        return firstChar.get();
    }
    public void setFirstChar(int value){
        this.firstChar.set(value);
    }
    @Override
    public int getLastChar() {
        return lastChar.get();
    }
    @Override
    public int getSpanOrder() {
        return getIndex();
    }

    public void setLastChar(int value){
        this.lastChar.set(value);
    }
    @Override
    public String getTagName(){
        return Span.splitTagName(getString());
    }
    void link(){
        stringReference.link();
    }
    void onRemoved(){
        stringReference.unlink();
        stringReference.set(-1);
    }
    @Override
    public String getSpanAttributes(){
        return Span.splitAttribute(getString());
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_tag, getString());
        jsonObject.put(NAME_first, getFirstChar());
        jsonObject.put(NAME_last, getLastChar());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setString(json.getString(NAME_tag));
        setFirstChar(json.getInt(NAME_first));
        setLastChar(json.getInt(NAME_last));
    }
    @Override
    public String toString() {
        return stringReference + " [" + getFirstChar() + ", " + getLastChar() + "]";
    }

    static class SpanStringReference extends ReferenceBlock<StyleSpan>{
        public SpanStringReference(StyleSpan styleSpan) {
            super(styleSpan, 0);
            set(-1);
        }
        public String getString(){
            StringItem stringItem = getStringItem();
            if(stringItem != null){
                return stringItem.get();
            }
            return null;
        }
        public void setString(String value) {
            unlink();
            StringPool<?> stringPool = getStringPool();
            StringItem stringItem = stringPool.getOrCreate(value);
            set(stringItem.getIndex());
            stringItem.addReference(this);
        }
        public void link(){
            StringItem stringItem = getStringItem();
            if(stringItem != null){
                stringItem.addReference(this);
            }
        }
        public void unlink(){
            StringItem stringItem = getStringItem();
            if(stringItem != null){
                stringItem.removeReference(this);
            }
        }
        private StringItem getStringItem() {
            StringPool<?> stringPool = getStringPool();
            if(stringPool != null){
                return stringPool.get(get());
            }
            return null;
        }
        private StringPool<?> getStringPool(){
            return getBlock().getParentInstance(StringPool.class);
        }

        @Override
        public String toString() {
            String value = getString();
            if(value != null){
                return value;
            }
            return "NULL{" + get() + "}";
        }
    }

    public static final String NAME_tag = ObjectsUtil.of("tag");
    public static final String NAME_first = ObjectsUtil.of("first");
    public static final String NAME_last = ObjectsUtil.of("last");
}

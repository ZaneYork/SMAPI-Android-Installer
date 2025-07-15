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

import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class StyleSpanEvent implements Comparable<StyleSpanEvent>{

    private final int type;
    private final char mChar;
    private final Span span;

    public StyleSpanEvent(int type, char ch, Span span){
        this.type = type;
        this.mChar = ch;
        this.span = span;
    }
    public StyleSpanEvent(char ch){
        this(TYPE_CHAR, ch, null);
    }
    public StyleSpanEvent(int type, Span span){
        this(type, (char)0, span);
    }

    public int getType() {
        return type;
    }
    public char getChar() {
        return mChar;
    }
    public Span getSpan() {
        return span;
    }

    public void serialize(XmlSerializer serializer) throws IOException {
        int type = getType();
        if(type == TYPE_CHAR){
            serializer.text(String.valueOf(getChar()));
        }else if(type == TYPE_START_END){
            Span span = getSpan();
            StyleElement element = span.toElement();
            String name = element.getName();
            serializer.startTag(null, name);
            Iterator<StyleAttribute> iterator = element.getAttributes();
            while (iterator.hasNext()){
                StyleAttribute attribute = iterator.next();
                attribute.serialize(serializer);
            }
            serializer.endTag(null, name);
        }else if(type == TYPE_START_TAG){
            Span span = getSpan();
            StyleElement element = span.toElement();
            serializer.startTag(null, element.getName());
            Iterator<StyleAttribute> iterator = element.getAttributes();
            while (iterator.hasNext()){
                StyleAttribute attribute = iterator.next();
                attribute.serialize(serializer);
            }
        }else if(type == TYPE_END_TAG){
            Span span = getSpan();
            serializer.endTag(null, span.getTagName());
        }else{
            throw new IOException("Unknown span event: " + type);
        }
    }
    @Override
    public int compareTo(StyleSpanEvent event) {
        if(event == this){
            return 0;
        }
        int type1 = getType();
        int type2 = event.getType();
        int j = CompareUtil.compare(type1, type2);
        if(type1 == TYPE_CHAR || type2 == TYPE_CHAR){
            return j;
        }
        int i = CompareUtil.compare(getSpan().getSpanOrder(), event.getSpan().getSpanOrder());
        if(type1 == TYPE_START_END){
            if(type2 == TYPE_END_TAG){
                return 1;
            }
            return i;
        }
        if(type2 == TYPE_START_END){
            if(type1 == TYPE_END_TAG){
                return 1;
            }
            return i;
        }
        if(i == 0){
            return j;
        }
        if(j == 0 && type1 == TYPE_END_TAG){
            i = -i;
        }
        return i;
    }

    @Override
    public String toString() {
        int type = getType();
        if(type == TYPE_CHAR){
            return String.valueOf(getChar());
        }
        if(type == TYPE_START_TAG){
            return "<" + getSpan().getTagName() + ">";
        }
        if(type == TYPE_START_END){
            return "<" + getSpan().getTagName() + "/>";
        }
        return "</" + getSpan().getTagName() + ">";
    }

    public static final int TYPE_START_END = ObjectsUtil.of(0);
    public static final int TYPE_START_TAG = ObjectsUtil.of(1);
    public static final int TYPE_CHAR = ObjectsUtil.of(2);
    public static final int TYPE_END_TAG = ObjectsUtil.of(3);

}

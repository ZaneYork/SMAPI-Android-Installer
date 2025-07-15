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

import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.EmptyList;

import java.util.List;

public interface Span {
    String getTagName();
    int getFirstChar();
    int getLastChar();
    int getSpanOrder();
    String getSpanAttributes();
    default StyleElement toElement(){
        StyleElement element = new StyleElement(getTagName());
        element.addAttributes(parseAttributes(getSpanAttributes()));
        return element;
    }
    default List<StyleAttribute> parseAttributes(String attributes){
        if(attributes == null || attributes.length() == 0){
            return EmptyList.of();
        }
        List<StyleAttribute> results = new ArrayCollection<>();
        StyleAttribute attribute = null;
        StringBuilder builder = new StringBuilder();
        int length = attributes.length();
        boolean quoted = false;
        for (int i = 0; i < length; i++){
            char ch = attributes.charAt(i);
            if(ch == '=' && attribute == null){
                if(builder != null){
                    attribute = new StyleAttribute(builder.toString(), "");
                    results.add(attribute);
                }
                builder = null;
                quoted = false;
                continue;
            }
            if(ch == '"' && attribute != null){
                if(quoted){
                    attribute.setValue(builder.toString());
                    attribute = null;
                    builder = null;
                }else if (builder == null){
                    quoted = true;
                    builder = new StringBuilder();
                }else {
                    builder.append(ch);
                }
                continue;
            }
            if(ch == ' ' || ch == ';'){
                if(attribute == null || builder == null){
                    attribute = null;
                    builder = null;
                    quoted = false;
                    continue;
                }
                if(quoted){
                    builder.append(ch);
                    continue;
                }
                attribute.setValue(builder.toString());
                attribute = null;
                builder = null;
                continue;
            }
            if(builder == null){
                builder = new StringBuilder();
            }
            builder.append(ch);
        }
        if(attribute != null && builder !=  null){
            attribute.setValue(builder.toString());
        }
        return results;
    }

    static String splitTagName(String raw){
        if(raw == null){
            return null;
        }
        int i = raw.indexOf(';');
        if(i < 0){
            i = raw.indexOf(' ');
        }
        if(i < 0){
            return raw;
        }
        return raw.substring(0, i);
    }
    static String splitAttribute(String tagWithAttribute){
        if(tagWithAttribute == null || tagWithAttribute.length() == 0){
            return "";
        }
        if(tagWithAttribute.charAt(0) == ' '){
            tagWithAttribute = tagWithAttribute.trim();
        }
        int i = tagWithAttribute.indexOf(';');
        int i2 = tagWithAttribute.indexOf(' ');
        if(i < 0){
            i = i2;
        }else if(i2 >= 0 && i2 < i){
            i = i2;
        }
        if(i < 0){
            return "";
        }
        return tagWithAttribute.substring(i + 1);
    }
}

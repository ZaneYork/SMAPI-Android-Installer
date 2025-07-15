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
package com.reandroid.dex.key;


import android.text.TextUtils;

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;

import java.io.IOException;
import java.util.Iterator;

public interface Key extends Comparable<Object>, SmaliFormat {

    default boolean uses(Key key){
        return this.equals(key) || CollectionUtil.contains(mentionedKeys(), key);
    }
    default TypeKey getDeclaring(){
        return TypeKey.NULL;
    }
    default Iterator<? extends Key> mentionedKeys(){
        throw new RuntimeException("Method 'mentionedKeys()' Not implemented for: " + getClass());
    }
    default Key replaceKey(Key search, Key replace){
        return this;
    }
    default boolean isPrimitiveKey() {
        return false;
    }
    default boolean isPlatform(){
        return DexUtils.isPlatform(getDeclaring());
    }
    default void append(SmaliWriter writer) throws IOException{
        writer.append(toString());
    }

    static Key parseBasic(String text){
        if(TextUtils.isEmpty(text)) {
            return null;
        }
        int i = text.indexOf('"');
        if(i == 0) {
            return StringKey.parseQuotedString(text);
        }
        i = text.indexOf('(');
        if(i > 0) {
            return MethodKey.parse(text);
        }
        i = text.indexOf(':');
        if(i > 0) {
            return FieldKey.parse(text);
        }
        return TypeKey.parse(text);
    }
    String DALVIK_accessFlags = ObjectsUtil.of("accessFlags");
    String DALVIK_name = ObjectsUtil.of("name");
    String DALVIK_value = ObjectsUtil.of("value");
}

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

import com.reandroid.dex.common.MethodHandleType;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public class MethodHandleKey implements Key{

    private final MethodHandleType handleType;
    private final Key member;

    public MethodHandleKey(MethodHandleType handleType, Key member){
        this.handleType = handleType;
        this.member = member;
    }

    public MethodHandleType getHandleType() {
        return handleType;
    }
    public Key getMember() {
        return member;
    }

    @Override
    public TypeKey getDeclaring() {
        return getMember().getDeclaring();
    }

    @Override
    public Iterator<? extends Key> mentionedKeys() {
        return getMember().mentionedKeys();
    }

    @Override
    public Key replaceKey(Key search, Key replace) {
        if(search.equals(this)){
            return replace;
        }
        Key key = getMember().replaceKey(search, replace);
        if(key != getMember()){
            return new MethodHandleKey(getHandleType(), key);
        }
        return this;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        append(writer, true);
    }
    public void append(SmaliWriter writer, boolean appendHandle) throws IOException {
        if (appendHandle) {
            getHandleType().append(writer);
        }
        writer.append('@');
        getMember().append(writer);
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        MethodHandleKey other = (MethodHandleKey) obj;
        int i = CompareUtil.compare(getHandleType(), other.getHandleType());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getMember(), other.getMember());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodHandleKey)) {
            return false;
        }
        MethodHandleKey other = (MethodHandleKey) obj;
        return ObjectsUtil.equals(getHandleType(), other.getHandleType()) &&
                ObjectsUtil.equals(getMember(), other.getMember());
    }
    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getHandleType(), getMember().hashCode());
    }

    @Override
    public String toString() {
        return getHandleType() + "@" + getMember();
    }

    public static MethodHandleKey read(SmaliReader reader) throws IOException {
        MethodHandleType handleType = MethodHandleType.read(reader);
        if (handleType == null) {
            return null;
        }
        SmaliParseException.expect(reader, '@');
        return read(handleType, reader);
    }

    public static MethodHandleKey read(MethodHandleType handleType, SmaliReader reader) throws IOException {
        Key key;
        if (handleType.isField()) {
            key = FieldKey.read(reader);
        } else {
            key = MethodKey.read(reader);
        }
        return new MethodHandleKey(handleType, key);
    }
}

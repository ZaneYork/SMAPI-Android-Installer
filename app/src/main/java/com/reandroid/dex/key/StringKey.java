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

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class StringKey implements Key{

    private final String text;
    private boolean mSignature;

    public StringKey(String text) {
        this.text = text;
    }

    public String getString() {
        return text;
    }
    public String getEncodedString() {
        return DexUtils.encodeString(getString());
    }
    public String getQuoted() {
        return DexUtils.quoteString(getString());
    }

    @Override
    public boolean isPlatform() {
        return false;
    }

    public boolean isSignature() {
        return mSignature;
    }
    public void setSignature(boolean signature) {
        this.mSignature = signature;
    }

    @Override
    public TypeKey getDeclaring() {
        if(!isSignature()){
            return null;
        }
        return TypeKey.parseSignature(getString());
    }
    @Override
    public Iterator<Key> mentionedKeys() {
        return CombiningIterator.singleOne(
                getDeclaring(),
                SingleIterator.of(this));
    }
    @Override
    public Key replaceKey(Key search, Key replace) {
        if(search.equals(this)){
            return replace;
        }
        return this;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        append(writer, false);
    }
    public void append(SmaliWriter writer, boolean enableComment) throws IOException {
        writer.append('"');
        boolean unicodeDetected = DexUtils.encodeString(writer, getString());
        writer.append('"');
        if(enableComment && unicodeDetected && writer.isCommentUnicodeStrings()) {
            DexUtils.appendCommentString(250, writer.getCommentAppender(), getString());
        }
    }
    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        StringKey key = (StringKey) obj;
        return CompareUtil.compare(getString(), key.getString());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StringKey)) {
            return false;
        }
        StringKey stringKey = (StringKey) obj;
        return ObjectsUtil.equals(getString(), stringKey.getString());
    }
    @Override
    public int hashCode() {
        return getString().hashCode();
    }
    @Override
    public String toString() {
        return getQuoted();
    }

    public static StringKey create(String text){
        if(text == null){
            return null;
        }
        if(text.length() == 0){
            return EMPTY;
        }
        return new StringKey(text);
    }
    public static StringKey parseQuotedString(String quotedString) {
        if(quotedString == null || quotedString.length() < 2) {
            return null;
        }
        SmaliReader reader = SmaliReader.of(quotedString);
        if(reader.get() != '\"') {
            return null;
        }
        String str;
        try {
            reader.skip(1);
            str = reader.readEscapedString('"');
            if(reader.available() != 1 || reader.get() != '\"') {
                return null;
            }
        } catch (IOException ignored) {
            return null;
        }
        return create(str);
    }
    public static StringKey read(SmaliReader reader) throws IOException{
        reader.skipSpaces();
        SmaliParseException.expect(reader, '\"');
        String str = reader.readEscapedString('"');
        SmaliParseException.expect(reader, '\"');
        return create(str);
    }

    public static final StringKey EMPTY = new StringKey(StringsUtil.EMPTY);
}

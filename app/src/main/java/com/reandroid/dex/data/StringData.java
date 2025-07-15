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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.base.OffsetReceiver;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.io.ByteReader;
import com.reandroid.dex.io.StreamUtil;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;
import java.io.OutputStream;

public class StringData extends DataItem
        implements SmaliFormat, BlockRefresh,
        OffsetSupplier, OffsetReceiver, Comparable<StringData> {

    private static final String EMPTY_STRING = StringsUtil.EMPTY;

    private final StringDataContainer mDataContainer;
    private String mCache;
    private StringKey mKey;

    public StringData() {
        super(1);
        this.mDataContainer = new StringDataContainer(this);
        addChild(0, mDataContainer);
        this.mCache = EMPTY_STRING;
    }

    public void removeSelf(){
        throw new DexException("Remove STRING_ID first before STRING_DATA");
    }
    public void removeSelf(StringId request){
        if(request != null && request.getParent() != null){
            super.removeSelf();
        }
    }

    @Override
    public StringKey getKey(){
        return mKey;
    }
    public void setKey(Key key){
        StringKey stringKey = (StringKey) key;
        String text = null;
        if(stringKey != null) {
            text = stringKey.getString();
        }
        setString(text);
    }
    @Override
    public SectionType<StringData> getSectionType() {
        return SectionType.STRING_DATA;
    }
    public String getString(){
        return mCache;
    }
    public void setString(String value){
        if(mCache.equals(value) && mKey != null){
            return;
        }
        if(value == null || value.length() == 0){
            value = EMPTY_STRING;
        }
        mCache = value;
        encodeString(value);
        StringKey key;
        if(value.length() == 0){
            key = StringKey.EMPTY;
        }else {
            key = new StringKey(value);
        }
        this.mKey = key;
    }
    public String getQuotedString() {
        return DexUtils.quoteString(getString());
    }

    void onStringBytesChanged() {
        String cache = decodeString();
        StringKey key;
        if(cache.length() == 0){
            key = StringKey.EMPTY;
        }else {
            key = new StringKey(cache);
        }
        this.mKey = key;
    }

    @Override
    public int countBytes() {
        return mDataContainer.countBytes();
    }
    @Override
    public byte[] getBytes() {
        return mDataContainer.getBytes();
    }

    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END == this){
            counter.FOUND=true;
            return;
        }
        counter.addCount(countBytes());
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        if(reader.available() < 4){
            return;
        }
        int position = reader.getPosition();
        String text = decodeString(StreamUtil.createByteReader(reader));
        if(text.length() == 0){
            text = EMPTY_STRING;
        }
        int length = reader.getPosition() - position;
        reader.seek(position);
        mDataContainer.setLength(length + 1);
        byte[] bytes = mDataContainer.getBytesInternal();
        reader.readFully(bytes);
        mCache = text;
        StringKey key;
        if(text.length() == 0){
            key = StringKey.EMPTY;
        }else {
            key = new StringKey(text);
        }
        this.mKey = key;
    }

    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        return mDataContainer.onWriteBytes(stream);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('"');
        boolean unicodeDetected = DexUtils.encodeString(writer, getString());
        writer.append('"');
        if(unicodeDetected && writer.isCommentUnicodeStrings()) {
            DexUtils.appendCommentString(250, writer.getCommentAppender(), getString());
        }
    }
    @Override
    public int compareTo(StringData stringData) {
        if(stringData == null){
            return -1;
        }
        if(stringData == this){
            return 0;
        }
        return getString().compareTo(stringData.getString());
    }
    @Override
    public String toString(){
        String text = getString();
        if(text != null){
            return text;
        }
        return "NULL";
    }
    private String decodeString(){
        String text;
        try {
            text = decodeString(StreamUtil.createByteReader(mDataContainer.getBytesInternal()));
        } catch (IOException exception) {
            text = null;
        }
        return text;
    }
    private void encodeString(String text){
        int length = text.length();
        mDataContainer.setLength(length * 3 + 4);
        final byte[] buffer = mDataContainer.getBytesInternal();
        int position = DexBlockItem.writeUleb128(buffer, 0, length);
        for (int i = 0; i < length; i++) {
            char ch = text.charAt(i);
            if ((ch != 0) && (ch < 0x80)) {
                buffer[position++] = (byte)ch;
            } else if (ch < 0x800) {
                buffer[position++] = (byte)(((ch >> 6) & 0x1f) | 0xc0);
                buffer[position++] = (byte)((ch & 0x3f) | 0x80);
            } else {
                buffer[position++] = (byte)(((ch >> 12) & 0x0f) | 0xe0);
                buffer[position++] = (byte)(((ch >> 6) & 0x3f) | 0x80);
                buffer[position++] = (byte)((ch & 0x3f) | 0x80);
            }
        }
        buffer[position++] = 0;
        mDataContainer.setLength(position);
    }
    private static String decodeString(ByteReader reader) throws IOException {
        int utf16Length = DexBlockItem.readUleb128(reader);
        char[] chars = new char[utf16Length];
        int outAt = 0;

        int at;
        for (at = 0; utf16Length > 0; utf16Length--) {
            int v0 = reader.read();
            char out;
            switch (v0 >> 4) {
                case 0x00: case 0x01: case 0x02: case 0x03:
                case 0x04: case 0x05: case 0x06: case 0x07: {
                    // 0XXXXXXX -- single-byte encoding
                    if (v0 == 0) {
                        // A single zero byte is illegal.
                        return throwBadUtf8(v0, at);
                    }
                    out = (char) v0;
                    at++;
                    break;
                }
                case 0x0c: case 0x0d: {
                    // 110XXXXX -- two-byte encoding
                    int v1 = reader.read() & 0xFF;
                    if ((v1 & 0xc0) != 0x80) {
                        return throwBadUtf8(v1, at + 1);
                    }
                    int value = ((v0 & 0x1f) << 6) | (v1 & 0x3f);
                    if ((value != 0) && (value < 0x80)) {
                        /*
                         * This should have been represented with
                         * one-byte encoding.
                         */
                        return throwBadUtf8(v1, at + 1);
                    }
                    out = (char) value;
                    at += 2;
                    break;
                }
                case 0x0e: {
                    // 1110XXXX -- three-byte encoding
                    int v1 = reader.read();
                    if ((v1 & 0xc0) != 0x80) {
                        return throwBadUtf8(v1, at + 1);
                    }
                    int v2 = reader.read();
                    if ((v2 & 0xc0) != 0x80) {
                        return throwBadUtf8(v2, at + 2);
                    }
                    int value = ((v0 & 0x0f) << 12) | ((v1 & 0x3f) << 6) |
                            (v2 & 0x3f);
                    if (value < 0x800) {
                        /*
                         * This should have been represented with one- or
                         * two-byte encoding.
                         */
                        return throwBadUtf8(v2, at + 2);
                    }
                    out = (char) value;
                    at += 3;
                    break;
                }
                default: {
                    // 10XXXXXX, 1111XXXX -- illegal
                    return throwBadUtf8(v0, at);
                }
            }
            chars[outAt] = out;
            outAt++;
        }
        return new String(chars, 0, outAt);
    }
    private static String throwBadUtf8(int value, int offset) throws IOException {
        throw new IOException("bad utf-8 byte " + HexUtil.toHex2("", (byte)value)
                + " at offset " + offset);
    }

    public static boolean equals(StringData stringData1, StringData stringData2) {
        return CompareUtil.compare(stringData1, stringData2) == 0;
    }
    static class StringDataContainer extends BlockItem {
        private final StringData stringData;

        StringDataContainer(StringData stringData) {
            super(0);
            this.stringData = stringData;
        }
        void setLength(int length){
            setBytesLength(length, false);
        }
        @Override
        public byte[] getBytesInternal() {
            return super.getBytesInternal();
        }
        @Override
        protected void onBytesChanged() {
            stringData.onStringBytesChanged();
        }
        @Override
        public int onWriteBytes(OutputStream stream) throws IOException {
            return super.onWriteBytes(stream);
        }
    }
}

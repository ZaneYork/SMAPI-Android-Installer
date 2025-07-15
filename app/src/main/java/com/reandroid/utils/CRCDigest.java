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
package com.reandroid.utils;

// implemented from http://www.libpng.org/pub/png/spec/1.2/PNG-CRCAppendix.html

import java.io.OutputStream;

public class CRCDigest extends OutputStream {

    private static final long[] CRC_TABLE = makeCrcTable();

    private long mCrc;
    private long mLength;
    private byte[] oneByte;

    public CRCDigest() {
        mCrc = 0xffffffffL;
    }

    public long getValue() {
        return mCrc ^ 0xffffffffL;
    }
    public long getLength() {
        return mLength;
    }

    @Override
    public void write(int i) {
        write(oneByte(i), 0, 1);
    }
    @Override
    public void write(byte[] buffer) {
        write(buffer, 0, buffer.length);
    }
    @Override
    public void write(byte[] buffer, int offset, int length) {
        long c = mCrc;
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            int b = buffer[i] & 0xff;
            c = CRC_TABLE[(int)((c ^ b) & 0xff)] ^ (c >> 8);
        }
        this.mCrc = c;
        this.mLength += length;
    }

    private byte[] oneByte(int i) {
        byte[] bytes = this.oneByte;
        if(bytes == null) {
            bytes = new byte[1];
            this.oneByte = bytes;
        }
        bytes[0] = (byte) i;
        return bytes;
    }

    public void update(int i) {
        write(oneByte(i), 0, 1);
    }
    public void update(byte[] buffer) {
        write(buffer, 0, buffer.length);
    }
    public void update(byte[] buffer, int offset, int length) {
        write(buffer, offset, length);
    }

    @Override
    public String toString() {
        return HexUtil.toHex(null, getValue(), 8);
    }

    private static long[] makeCrcTable() {
        long[] table = new long[256];
        for (int i = 0; i < 256; i++) {
            long c = i;
            for (int j = 0; j < 8; j++) {
                if ((c & 1) == 1) {
                    c = 0xedb88320L ^ (c >> 1);
                } else {
                    c = c >> 1;
                }
            }
            table[i] = c;
        }
        return table;
    }
}

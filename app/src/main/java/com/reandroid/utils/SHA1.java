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

public class SHA1 {

    private static final int round1_kt = 0x5A827999;
    private static final int round2_kt = 0x6ED9EBA1;
    private static final int round3_kt = 0x8F1BBCDC;
    private static final int round4_kt = 0xCA62C1D6;

    private final byte[] padding;
    private final int[] WORD;
    private final int[] state;
    private byte[] oneByte;
    private final byte[] buffer;
    private int bufferOffset;
    private long bytesProcessed;

    public SHA1() {
        this.padding = new byte[136];
        this.buffer = new byte[64];
        this.WORD = new int[80];
        this.state = new int[5];
        this.padding[0] = (byte) 0x80;
        this.resetState();
    }
    public void update(byte b) {
        if (this.oneByte == null) {
            this.oneByte = new byte[1];
        }
        this.oneByte[0] = b;
        this.update(this.oneByte, 0, 1);
    }
    public void update(byte[] bytes, int offset, int length) {
        if (this.bytesProcessed < 0L) {
            this.reset();
        }
        this.bytesProcessed += length;
        int limit;
        if (this.bufferOffset != 0) {
            limit = NumbersUtil.min(length, 64 - this.bufferOffset);
            System.arraycopy(bytes, offset, this.buffer, this.bufferOffset, limit);
            this.bufferOffset += limit;
            offset += limit;
            length -= limit;
            if (this.bufferOffset >= 64) {
                this.compressBytes(this.buffer, 0);
                this.bufferOffset = 0;
            }
        }

        if (length >= 64) {
            limit = offset + length;
            offset = this.compressMultiBlock(bytes, offset, limit - 64);
            length = limit - offset;
        }

        if (length > 0) {
            System.arraycopy(bytes, offset, this.buffer, 0, length);
            this.bufferOffset = length;
        }

    }
    public void reset() {
        if (this.bytesProcessed != 0L) {
            this.resetState();
            fillZero(this.WORD);
            this.bufferOffset = 0;
            this.bytesProcessed = 0L;
            fillZero(this.buffer);
        }
    }

    public byte[] digest() {
        byte[] b = new byte[20];
        this.digest(b);
        return b;
    }
    public void digest(byte[] out) {
        this.digest(out, 0);
    }
    public void digest(byte[] out, int offset) {
        if (this.bytesProcessed < 0L) {
            this.reset();
        }
        this.digestBytes(out, offset);
        this.bytesProcessed = -1L;
    }

    private void digestBytes(byte[] out, int offset) {
        long bitsProcessed = this.bytesProcessed << 3;
        int index = (int)this.bytesProcessed & 63;
        int padLen = index < 56 ? 56 - index : 120 - index;
        this.update(padding, 0, padLen);
        writeBigInt((int)(bitsProcessed >>> 32), this.buffer, 56);
        writeBigInt((int)bitsProcessed, this.buffer, 60);
        this.compressBytes(this.buffer, 0);
        writeBigInt(this.state, out, offset);
    }

    private void resetState() {
        int[] state = this.state;
        state[0] = 0x67452301;
        state[1] = 0xEFCDAB89;
        state[2] = 0x98BADCFE;
        state[3] = 0x10325476;
        state[4] = 0xC3D2E1F0;
    }
    private int compressMultiBlock(byte[] bytes, int offset, int limit) {
        while(offset <= limit) {
            this.compressBytes(bytes, offset);
            offset += 64;
        }
        return offset;
    }

    private void compressBytes(byte[] buf, int offset) {
        int[] word = this.WORD;
        for(int i = 0; i < 16; i++) {
            word[i] = getReversedInteger(buf, (offset + i * 4));
        }
        this.compressWord(word);
    }
    private void compressWord(int[] word) {
        int a;
        int b;
        for(a = 16; a <= 79; ++a) {
            b = word[a - 3] ^ word[a - 8] ^ word[a - 14] ^ word[a - 16];
            word[a] = b << 1 | b >>> 31;
        }
        int[] state = this.state;
        a = state[0];
        b = state[1];
        int c = state[2];
        int d = state[3];
        int e = state[4];

        int i;
        int temp;
        for(i = 0; i < 20; ++i) {
            temp = (a << 5 | a >>> 27) + (b & c | ~b & d) + e + word[i] + round1_kt;
            e = d;
            d = c;
            c = b << 30 | b >>> 2;
            b = a;
            a = temp;
        }

        for(i = 20; i < 40; ++i) {
            temp = (a << 5 | a >>> 27) + (b ^ c ^ d) + e + word[i] + round2_kt;
            e = d;
            d = c;
            c = b << 30 | b >>> 2;
            b = a;
            a = temp;
        }

        for(i = 40; i < 60; ++i) {
            temp = (a << 5 | a >>> 27) + (b & c | b & d | c & d) + e + word[i] + round3_kt;
            e = d;
            d = c;
            c = b << 30 | b >>> 2;
            b = a;
            a = temp;
        }

        for(i = 60; i < 80; ++i) {
            temp = (a << 5 | a >>> 27) + (b ^ c ^ d) + e + word[i] + round4_kt;
            e = d;
            d = c;
            c = b << 30 | b >>> 2;
            b = a;
            a = temp;
        }
        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
        state[4] += e;
    }
    private void writeBigInt(int[] in, byte[] out, int offset) {
        int length = offset + 20;
        int i = 0;
        while (offset < length) {
            putReversedInteger(out, offset, in[i]);
            offset += 4;
            i ++;
        }
    }
    private void writeBigInt(int value, byte[] out, int offset) {
        putReversedInteger(out, offset, value);
    }
    private int getReversedInteger(byte[] bytes, int offset){
        return bytes[offset + 3] & 0xff |
                (bytes[offset + 2] & 0xff) << 8 |
                (bytes[offset + 1] & 0xff) << 16 |
                (bytes[offset] & 0xff) << 24;
    }
    private void putReversedInteger(byte[] bytes, int offset, int val){
        bytes[offset] = (byte) (val >>> 24 );
        bytes[offset + 1] = (byte) (val >>> 16);
        bytes[offset + 2] = (byte) (val >>> 8 );
        bytes[offset + 3] = (byte) (val & 0xff);
    }
    private void fillZero(int[] arr) {
        int length = arr.length;
        for(int i = 0; i < length; i++){
            arr[i] = 0;
        }
    }
    private void fillZero(byte[] arr) {
        int length = arr.length;
        for(int i = 0; i < length; i++){
            arr[i] = (byte) 0;
        }
    }
}

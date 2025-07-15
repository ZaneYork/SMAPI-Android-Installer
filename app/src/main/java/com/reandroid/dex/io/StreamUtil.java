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
package com.reandroid.dex.io;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {

    public static ByteReader createByteReader(byte[] bytes, int offset){
        return new ByteArrayReader(bytes, offset);
    }
    public static ByteReader createByteReader(byte[] bytes){
        return new ByteArrayReader(bytes, 0);
    }
    public static ByteReader createByteReader(InputStream inputStream){
        return new ByteInputStreamReader(inputStream);
    }

    static class ByteInputStreamReader implements ByteReader {
        private final InputStream inputStream;
        private int count;
        ByteInputStreamReader(InputStream inputStream){
            this.inputStream = inputStream;
        }
        @Override
        public int read() throws IOException {
            int i = inputStream.read();
            if(i == -1){
                throw new IOException("Finished reading: " + inputStream);
            }
            count ++;
            return i;
        }
        @Override
        public int count() {
            return count;
        }
    }
    static class ByteArrayReader implements ByteReader {
        private final byte[] bytes;
        private final int offset;
        private int index;
        ByteArrayReader(byte[] bytes, int offset){
            this.bytes = bytes;
            this.offset = offset;
        }
        @Override
        public int read() throws IOException{
            int i = bytes[offset + index] & 0xFF;
            index++;
            return i;
        }
        @Override
        public int count() {
            return index;
        }
    }
}

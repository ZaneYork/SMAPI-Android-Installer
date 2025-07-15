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
package com.reandroid.common;

public interface ByteSource {

    byte read(int i);
    void read(int position, byte[] buffer, int offset, int length);
    int length();

    default int indexOf(int start, byte b){
        int end = length();
        int last = end - 1;
        for(int i = start; i < end; i++){
            if(b == read(i)){
                return i;
            }
            if(i == last) {
                end = length();
                last = end - 1;
            }
        }
        return -1;
    }

    static ByteSource of(byte[] bytes) {
        return of(bytes, 0, bytes.length);
    }
    static ByteSource of(byte[] bytes, int start, int size) {
        return new ByteSource() {
            @Override
            public byte read(int i) {
                return bytes[i];
            }
            @Override
            public void read(int position, byte[] buffer, int offset, int length) {
                System.arraycopy(bytes, start + position, buffer, offset, length);
            }
            @Override
            public int length() {
                return size;
            }
        };
    }
}

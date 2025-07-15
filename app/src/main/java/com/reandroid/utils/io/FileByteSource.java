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
package com.reandroid.utils.io;

import com.reandroid.common.ByteSource;
import com.reandroid.common.FileChannelInputStream;

import java.io.File;
import java.io.IOException;

public class FileByteSource implements ByteSource {

    private byte[] array;
    private File file;
    private int length;

    public FileByteSource() {
    }

    public File getFile() {
        return file;
    }
    public void setFile(File file) throws IOException {
        this.file = file;
        int length = (int) file.length();
        byte[] array = this.array;
        if(array == null || length > array.length) {
            array = new byte[length];
            this.array = array;
        }
        this.length = length;
        FileChannelInputStream.read(file, array, length);
    }

    @Override
    public byte read(int i) {
        if(i > this.length) {
            throw new IndexOutOfBoundsException("Out of range: " + i + ", max = " + this.length);
        }
        return array[i];
    }

    @Override
    public void read(int position, byte[] buffer, int offset, int length) {
        if(position + length > this.length) {
            length = length + position;
            throw new IndexOutOfBoundsException("Out of range: " + length + ", max = " + this.length);
        }
        System.arraycopy(this.array, position, buffer, offset, length);
    }

    @Override
    public int length() {
        return length;
    }
}

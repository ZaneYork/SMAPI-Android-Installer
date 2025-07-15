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
package com.reandroid.archive.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ZipStreamOutput extends ZipOutput {
    private final CountingOutputStream<OutputStream> countingStream;
    public ZipStreamOutput(OutputStream outputStream){
        this.countingStream = new CountingOutputStream<>(outputStream, true);
    }
    @Override
    public long position() throws IOException {
        return countingStream.getSize();
    }
    @Override
    public void position(long pos) throws IOException {
        throw new IOException("Can not move position of ZipStreamOutput");
    }
    @Override
    public void close() throws IOException {
        countingStream.close();
    }
    @Override
    public boolean isOpen() {
        return countingStream.isOpen();
    }
    @Override
    public void write(InputStream inputStream) throws IOException {
        countingStream.write(inputStream);
    }
    public void write(byte[] bytes) throws IOException {
        countingStream.write(bytes);
    }
    @Override
    public OutputStream getOutputStream(){
        return countingStream;
    }
}

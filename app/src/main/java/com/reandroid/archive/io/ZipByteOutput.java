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

import com.reandroid.common.BytesOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ZipByteOutput extends ZipOutput{
    private final BytesOutputStream bis;
    public ZipByteOutput(){
        this.bis = new BytesOutputStream();
    }
    public byte[] toByteArray(){
        return bis.toByteArray();
    }
    @Override
    public long position() throws IOException {
        return bis.position();
    }
    @Override
    public void position(long pos) throws IOException {
        throw new IOException("Not used");
    }
    @Override
    public void close() throws IOException {
        bis.close();
    }
    @Override
    public boolean isOpen() {
        return true;
    }
    @Override
    public void write(InputStream inputStream) throws IOException {
        bis.write(inputStream);
    }
    public void write(byte[] bytes) throws IOException {
        bis.write(bytes);
    }
    @Override
    public OutputStream getOutputStream() {
        return bis;
    }
}

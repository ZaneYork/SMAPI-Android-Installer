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
package com.reandroid.archive;

import com.reandroid.common.FileChannelInputStream;

import java.io.*;

public class FileInputSource extends InputSource {
    private final File file;
    public FileInputSource(File file, String name){
        super(name);
        this.file=file;
    }
    @Override
    public byte[] getBytes(int length) throws IOException{
        return FileChannelInputStream.read(getFile(), length);
    }
    @Override
    public long getLength() {
        return getFile().length();
    }
    @Override
    public void close(InputStream inputStream) throws IOException {
        inputStream.close();
    }
    @Override
    public FileChannelInputStream openStream() throws IOException {
        return new FileChannelInputStream(this.file);
    }
    public File getFile(){
        return file;
    }

}

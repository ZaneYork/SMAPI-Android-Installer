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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelOutputStream extends OutputStream {
    private final FileChannel fileChannel;
    public FileChannelOutputStream(FileChannel fileChannel){
        this.fileChannel = fileChannel;
    }
    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }
    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        long position = fileChannel.position();
        length = fileChannel.write(ByteBuffer.wrap(bytes, offset, length));
        fileChannel.position(position + length);
    }
    @Override
    public void write(int i) throws IOException {
        byte b = (byte) (i & 0xff);
        write(new byte[]{b});
    }
    @Override
    public void close(){

    }
}

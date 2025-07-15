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

import com.reandroid.common.BytesInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteInputSource extends InputSource {
    private byte[] array;
    public ByteInputSource(byte[] array, String name) {
        super(name);
        this.array = array;
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        byte[] bytes = getBytes();
        outputStream.write(bytes);
        return bytes.length;
    }
    @Override
    public InputStream openStream() throws IOException {
        return new BytesInputStream(getBytes());
    }
    public byte[] getBytes() {
        return array;
    }
    @Override
    public void disposeInputSource(){
        array = new byte[0];
    }
}

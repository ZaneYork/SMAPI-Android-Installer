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
package com.reandroid.archive.writer;

import com.reandroid.archive.InputSource;
import com.reandroid.archive.io.ZipByteOutput;

import java.io.IOException;

/**
 * Writes full apk (zip) file to byte array
 * */
public class ApkByteWriter extends ApkWriter<ZipByteOutput, ByteOutputSource>{
    public ApkByteWriter(ZipByteOutput zipOutput, InputSource[] sources) {
        super(zipOutput, sources);
    }
    public ApkByteWriter(InputSource[] sources) {
        this(new ZipByteOutput(), sources);
    }

    public byte[] toByteArray(){
        return getZipOutput().toByteArray();
    }
    @Override
    void writeApk(ByteOutputSource outputSource, ZipAligner zipAligner) throws IOException {
        outputSource.writeApk(getZipOutput(), zipAligner);
    }
    @Override
    void prepareOutputs(ByteOutputSource[] outList) throws IOException {
    }
    @Override
    ByteOutputSource toOutputSource(InputSource inputSource) {
        return new ByteOutputSource(inputSource);
    }
    @Override
    ByteOutputSource[] createOutArray(int length) {
        return new ByteOutputSource[length];
    }
}

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
import com.reandroid.archive.io.ZipStreamOutput;

import java.io.IOException;
import java.io.OutputStream;

public class ApkStreamWriter extends ApkWriter<ZipStreamOutput, StreamOutputSource>{
    public ApkStreamWriter(ZipStreamOutput zipOutput, InputSource[] sources) {
        super(zipOutput, sources);
    }
    public ApkStreamWriter(OutputStream outputStream, InputSource[] sources) {
        this(new ZipStreamOutput(outputStream), sources);
    }
    @Override
    void writeApk(StreamOutputSource outputSource, ZipAligner zipAligner) throws IOException {
        outputSource.writeApk(getZipOutput(), zipAligner);
    }
    @Override
    void prepareOutputs(StreamOutputSource[] outList) throws IOException {
    }
    @Override
    StreamOutputSource toOutputSource(InputSource inputSource) {
        return new StreamOutputSource(inputSource);
    }
    @Override
    StreamOutputSource[] createOutArray(int length) {
        return new StreamOutputSource[length];
    }
}

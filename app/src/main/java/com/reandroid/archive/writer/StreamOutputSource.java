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
import com.reandroid.archive.io.ZipStreamOutput;

import java.io.IOException;

class StreamOutputSource extends OutputSource{
    StreamOutputSource(InputSource inputSource) {
        super(inputSource);
    }
    void writeApk(ZipStreamOutput zipOutput, ZipAligner zipAligner) throws IOException {
        ZipByteOutput buffer = new ZipByteOutput();
        writeBuffer(buffer);
        buffer.close();

        writeLFH(zipOutput, zipAligner);
        getLocalFileHeader().setFileOffset(zipOutput.position());
        zipOutput.write(buffer.toByteArray());
        writeDD(zipOutput);
    }
}

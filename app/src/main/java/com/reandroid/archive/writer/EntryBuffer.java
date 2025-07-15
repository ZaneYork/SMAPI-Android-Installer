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

import com.reandroid.archive.io.ZipFileInput;

public class EntryBuffer {
    private final ZipFileInput zipFileInput;
    private final long offset;
    private final long length;
    public EntryBuffer(ZipFileInput zipFileInput, long offset, long length){
        this.zipFileInput = zipFileInput;
        this.offset = offset;
        this.length = length;
    }

    public ZipFileInput getZipFileInput() {
        return zipFileInput;
    }
    public long getOffset() {
        return offset;
    }
    public long getLength() {
        return length;
    }

}

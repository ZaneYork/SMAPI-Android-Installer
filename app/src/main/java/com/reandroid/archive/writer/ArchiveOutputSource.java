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
import com.reandroid.archive.block.LocalFileHeader;
import com.reandroid.archive.io.ArchiveFileEntrySource;
import com.reandroid.archive.io.ZipFileInput;


public class ArchiveOutputSource extends FileOutputSource {
    public ArchiveOutputSource(InputSource inputSource){
        super(inputSource);
    }

    ArchiveFileEntrySource getArchiveSource(){
        return (ArchiveFileEntrySource) super.getInputSource();
    }
    @Override
    EntryBuffer makeFromEntry(){
        ArchiveFileEntrySource entrySource = getArchiveSource();
        ZipFileInput zipFileInput = entrySource.getZipSource();
        LocalFileHeader lfh = entrySource.getArchiveEntry().getLocalFileHeader();
        if(lfh.getMethod() != getInputSource().getMethod()){
            return null;
        }
        return new EntryBuffer(zipFileInput,
                lfh.getFileOffset(),
                lfh.getDataSize());
    }
    @Override
    LocalFileHeader createLocalFileHeader(){
        ArchiveFileEntrySource source = getArchiveSource();
        LocalFileHeader lfh = source.getArchiveEntry().getLocalFileHeader();
        return lfh.copy();
    }
}

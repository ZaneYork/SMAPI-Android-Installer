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

import com.abdurazaaqmohammed.AntiSplit.main.LegacyUtils;
import com.reandroid.archive.Archive;
import com.reandroid.archive.ArchiveEntry;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class ArchiveFileEntrySource extends ArchiveEntrySource<ZipFileInput> {

    public ArchiveFileEntrySource(ZipFileInput zipInput, ArchiveEntry archiveEntry){
        super(zipInput, archiveEntry);
        setSort(archiveEntry.getIndex());
    }

    @Override
    public byte[] getBytes(int length) throws IOException {
        FileChannel fileChannel = getFileChannel();
        if(getMethod() != Archive.STORED || fileChannel == null){
            return super.getBytes(length);
        }
        byte[] bytes = new byte[length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        fileChannel.read(byteBuffer);
        return bytes;
    }

    FileChannel getFileChannel() throws IOException {
        ZipFileInput zipInput = getZipSource();
        FileChannel fileChannel = zipInput.getFileChannel();
        fileChannel.position(getArchiveEntry().getFileOffset());
        return fileChannel;
    }

    @Override
    public void write(File file) throws IOException {
        FileChannel fileChannel = getFileChannel();
        if(getMethod() != Archive.STORED || fileChannel == null){
            super.write(file);
            return;
        }
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()) dir.mkdirs();
        if(file.isFile()) file.delete();
        file.createNewFile();
        if (LegacyUtils.supportsFileChannel) write(fileChannel, FileChannel.open(file.toPath(), StandardOpenOption.WRITE));
        else try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            write(fileChannel, raf.getChannel());
        }
    }

    private void write(FileChannel fileChannel, FileChannel outputChannel) throws IOException {
        outputChannel.transferFrom(fileChannel, 0, getLength());
        outputChannel.close();
    }
}
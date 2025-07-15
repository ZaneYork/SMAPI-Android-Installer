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
package com.reandroid.archive.model;

import com.reandroid.archive.ArchiveEntry;
import com.reandroid.archive.ArchiveException;
import com.reandroid.archive.block.*;
import com.reandroid.archive.block.ApkSignatureBlock;
import com.reandroid.archive.io.ZipInput;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalFileDirectory {
    private final CentralFileDirectory centralFileDirectory;
    private final List<LocalFileHeader> headerList;
    private ApkSignatureBlock apkSignatureBlock;
    public LocalFileDirectory(CentralFileDirectory centralFileDirectory){
        this.centralFileDirectory = centralFileDirectory;
        this.headerList = new ArrayList<>(centralFileDirectory.count() + 2);
    }
    public void visit(ZipInput zipInput) throws IOException {
        visitLocalFile(zipInput);
        visitApkSigBlock(zipInput);
    }
    private void visitLocalFile(ZipInput zipInput) throws IOException {
        List<LocalFileHeader> headerList = this.getHeaderList();
        long offset;
        int index = 0;
        CentralFileDirectory centralFileDirectory = getCentralFileDirectory();
        long length = zipInput.getLength();
        InputStream inputStream = zipInput.getInputStream(0, length);
        for(CentralEntryHeader ceh : centralFileDirectory.getHeaderList()){
            offset = ceh.getLocalRelativeOffset();
            inputStream.reset();
            offset = inputStream.skip(offset);
            LocalFileHeader lfh = LocalFileHeader.read(inputStream);
            if(lfh == null){
                throw new ArchiveException("Error reading LFH at "
                        + offset + ", for CEH = " + ceh.getFileName());
            }
            offset = offset + lfh.countBytes();
            ceh.setFileOffset(offset);

            lfh.setCentralEntryHeader(ceh);

            inputStream.skip(lfh.getDataSize());

            lfh.updateDataDescriptor();
            DataDescriptor dataDescriptor = lfh.getDataDescriptor();
            if(dataDescriptor != null) {
                int read = dataDescriptor.readBytes(inputStream);
                if(read != dataDescriptor.countBytes()) {
                    lfh.setHasDataDescriptor(false);
                }
            }
            lfh.setIndex(index);

            headerList.add(lfh);

            index++;
        }
    }
    private void visitApkSigBlock(ZipInput zipInput) throws IOException{
        CentralFileDirectory cfd = getCentralFileDirectory();
        SignatureFooter footer = cfd.getSignatureFooter();
        if(footer == null || !footer.isValid()){
            return;
        }
        EndRecord endRecord = cfd.getEndRecord();
        long length = footer.getSignatureSize() + 8;
        long offset = endRecord.getOffsetOfCentralDirectory() - length;
        ApkSignatureBlock apkSignatureBlock = new ApkSignatureBlock(footer);
        apkSignatureBlock.readBytes(new BlockReader(zipInput.getInputStream(offset, length)));
        this.apkSignatureBlock = apkSignatureBlock;
    }
    public ApkSignatureBlock getApkSigBlock() {
        return apkSignatureBlock;
    }
    public CentralFileDirectory getCentralFileDirectory() {
        return centralFileDirectory;
    }
    public List<LocalFileHeader> getHeaderList() {
        return headerList;
    }
    public ArchiveEntry[] buildArchiveEntryList(){
        List<LocalFileHeader> headerList = getHeaderList();
        int size = headerList.size();
        ArchiveEntry[] entryList = new ArchiveEntry[size];
        for(int i = 0; i < size; i++){
            LocalFileHeader lfh = headerList.get(i);
            CentralEntryHeader ceh = lfh.getCentralEntryHeader();
            if(ceh == null){
                continue;
            }
            entryList[i] = new ArchiveEntry(lfh);
        }
        return entryList;
    }
}

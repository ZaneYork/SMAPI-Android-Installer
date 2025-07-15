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

import com.reandroid.archive.block.CentralEntryHeader;
import com.reandroid.archive.block.LocalFileHeader;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.io.FilePermissions;

public class ArchiveEntry {
    private final LocalFileHeader localFileHeader;
    public ArchiveEntry(LocalFileHeader lfh){
        this.localFileHeader = lfh;
    }
    public long getDataSize(){
        if(getMethod() != Archive.DEFLATED){
            return getSize();
        }
        return getCompressedSize();
    }

    public boolean isCompressed(){
        return getMethod() == Archive.DEFLATED;
    }
    public int getMethod(){
        return localFileHeader.getMethod();
    }
    public void setMethod(int method){
        localFileHeader.setMethod(method);
        getCentralEntryHeader().setMethod(method);
    }
    public long getSize() {
        return localFileHeader.getSize();
    }
    public void setSize(long size) {
        localFileHeader.setSize(size);
        getCentralEntryHeader().setSize(size);
    }
    public long getCrc() {
        return localFileHeader.getCrc();
    }
    public void setCrc(long crc) {
        localFileHeader.setCrc(crc);
        getCentralEntryHeader().setCrc(crc);
    }
    public long getCompressedSize() {
        return localFileHeader.getCompressedSize();
    }
    public void setCompressedSize(long csize) {
        localFileHeader.setCompressedSize(csize);
        getCentralEntryHeader().setCompressedSize(csize);
    }
    public long getFileOffset() {
        return localFileHeader.getFileOffset();
    }
    public String getName(){
        return localFileHeader.getFileName();
    }
    public String getSanitizedName(){
        String name = ArchiveUtil.sanitizePath(localFileHeader.getFileName());
        if(name == null){
            name = ".error_file_path_" + localFileHeader.getIndex();
        }
        return name;
    }
    public void setName(String name){
        localFileHeader.setFileName(name);
        getCentralEntryHeader().setFileName(name);
    }
    public String getComment(){
        return getCentralEntryHeader().getComment();
    }
    public void setComment(String comment){
        getCentralEntryHeader().setComment(comment);
    }
    public boolean isFile() {
        return !isDirectory();
    }
    public boolean isDirectory() {
        return getDataSize() == 0 && this.getName().endsWith("/");
    }
    public FilePermissions getFilePermissions() {
        return getCentralEntryHeader().getFilePermissions();
    }
    public CentralEntryHeader getCentralEntryHeader(){
        CentralEntryHeader ceh = localFileHeader.getCentralEntryHeader();
        if(ceh == null){
            ceh = CentralEntryHeader.fromLocalFileHeader(localFileHeader);
            localFileHeader.setCentralEntryHeader(ceh);
        }
        return ceh;
    }
    public LocalFileHeader getLocalFileHeader() {
        return localFileHeader;
    }
    public long getDosTime(){
        return getCentralEntryHeader().getDosTime();
    }
    public void setDosTime(long dosTime){
        getCentralEntryHeader().setDosTime(dosTime);
        getLocalFileHeader().setDosTime(dosTime);
    }
    public int getIndex(){
        return getCentralEntryHeader().getIndex();
    }
    @Override
    public String toString(){
        return "["+ getFileOffset()+"] " + getName() + getComment()
                + HexUtil.toHex(" 0x", getCrc(), 8);
    }
}

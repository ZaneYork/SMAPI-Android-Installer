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
package com.reandroid.archive.block;

import com.reandroid.archive.ZipSignature;

import java.io.IOException;
import java.io.InputStream;

public class LocalFileHeader extends CommonHeader {

    private DataDescriptor dataDescriptor;
    private CentralEntryHeader centralEntryHeader;

    public LocalFileHeader(){
        super(OFFSET_fileName, ZipSignature.LOCAL_FILE, OFFSET_general_purpose);
    }
    public LocalFileHeader(String name){
        this();
        setFileName(name);
    }

    public CentralEntryHeader getCentralEntryHeader() {
        return centralEntryHeader;
    }
    public void setCentralEntryHeader(CentralEntryHeader ceh) {
        this.centralEntryHeader = ceh;
        if(ceh == null){
            return;
        }
        mergeZeroValues(ceh);
    }

    public LocalFileHeader copy(){
        LocalFileHeader lfh = new LocalFileHeader();
        lfh.setSignature(ZipSignature.LOCAL_FILE);
        lfh.setFileName(getFileName());
        lfh.getGeneralPurposeFlag().setValue(getGeneralPurposeFlag().getValue());
        lfh.setCompressedSize(getCompressedSize());
        lfh.setSize(getSize());
        lfh.setCrc(getCrc());
        lfh.setDosTime(getDosTime());
        lfh.setPlatform(getPlatform());
        lfh.setVersionMadeBy(getVersionMadeBy());
        lfh.setMethod(getMethod());
        lfh.updateDataDescriptor();
        return lfh;
    }

    public void mergeZeroValues(CentralEntryHeader ceh){
        if(getFileOffset() == 0){
            setFileOffset(ceh.getFileOffset());
        }
        if(getCrc()==0){
            setCrc(ceh.getCrc());
        }
        if(getSize()==0){
            setSize(ceh.getSize());
        }
        if(getCompressedSize()==0){
            setCompressedSize(ceh.getCompressedSize());
        }
        if(getGeneralPurposeFlag().getValue()==0){
            getGeneralPurposeFlag().setValue(ceh.getGeneralPurposeFlag().getValue());
        }
    }

    @Override
    public long getCompressedSize() {
        DataDescriptor dataDescriptor = getDataDescriptor();
        if(dataDescriptor != null) {
            return dataDescriptor.getCompressedSize();
        }
        return getCompressedSizeInternal();
    }
    private long getCompressedSizeInternal(){
        if(isZip64()){
            return getZip64CompressedSize();
        }
        return getIntegerUnsigned(getOffsetCompressedSize());
    }
    @Override
    public void setCompressedSize(long value) {
        DataDescriptor dataDescriptor = getDataDescriptor();
        if (dataDescriptor != null) {
            dataDescriptor.setCompressedSize(value);
            setCompressedSizeInternal(0);
        } else {
            setCompressedSizeInternal(value);
        }
    }
    private void setCompressedSizeInternal(long value){
        if (isZip64Value() || isZip64Value(value)){
            ensureZip64();
            putInteger(getOffsetCompressedSize(), -1);
            setZip64CompressedSize(value);
        } else {
            putInteger(getOffsetCompressedSize(), value);
        }
    }
    @Override
    public void setCrc(long value) {
        DataDescriptor dataDescriptor = getDataDescriptor();
        if (dataDescriptor != null) {
            dataDescriptor.setCrc(value);
            setCrcInternal(0);
        } else {
            setCrcInternal(value);
        }
    }
    private void setCrcInternal(long value) {
        super.setCrc(value);
    }

    @Override
    public long getSize() {
        DataDescriptor dataDescriptor = getDataDescriptor();
        if (dataDescriptor != null) {
            return dataDescriptor.getSize();
        }
        return getSizeInternal();
    }
    private long getSizeInternal() {
        if (isZip64()){
            return getZip64Size();
        }
        return getIntegerUnsigned(getOffsetSize());
    }
    @Override
    public void setSize(long value) {
        DataDescriptor dataDescriptor = getDataDescriptor();
        if (dataDescriptor != null) {
            dataDescriptor.setSize(value);
            setSizeInternal(0);
        } else {
            setSizeInternal(value);
        }
    }
    private void setSizeInternal(long value){
        if (isZip64Value() || isZip64Value(value)){
            ensureZip64();
            putInteger(getOffsetSize(), -1);
            setZip64CompressedSize(value);
        } else {
            putInteger(getOffsetSize(), value);
        }
    }
    @Override
    public long getCrc() {
        DataDescriptor dataDescriptor = getDataDescriptor();
        if (dataDescriptor != null) {
            return dataDescriptor.getCrc();
        }
        return super.getCrc();
    }

    public DataDescriptor getDataDescriptor() {
        return dataDescriptor;
    }

    @Override
    public void setHasDataDescriptor(boolean hasDataDescriptor) {
        if(hasDataDescriptor != this.hasDataDescriptor() ||
                hasDataDescriptor == (getDataDescriptor() == null)) {
            super.setHasDataDescriptor(hasDataDescriptor);
            updateDataDescriptor();
        }
    }

    public void updateDataDescriptor() {
        DataDescriptor dataDescriptor = this.dataDescriptor;
        if (hasDataDescriptor()) {
            if(dataDescriptor == null) {
                this.dataDescriptor = DataDescriptor.fromLocalFile(this);
                setCrcInternal(0);
                setCompressedSizeInternal(0);
                setSizeInternal(0);
            }
        } else {
            this.dataDescriptor = null;
            if(dataDescriptor != null) {
                setCrcInternal(dataDescriptor.getCrc());
                setSizeInternal(dataDescriptor.getSize());
                setCompressedSizeInternal(dataDescriptor.getCompressedSize());
            }
        }
    }

    public static LocalFileHeader fromCentralEntryHeader(CentralEntryHeader ceh){
        LocalFileHeader lfh = new LocalFileHeader();
        lfh.setSignature(ZipSignature.LOCAL_FILE);
        lfh.setVersionMadeBy(ceh.getVersionMadeBy());
        lfh.getGeneralPurposeFlag().setValue(ceh.getGeneralPurposeFlag().getValue());
        lfh.setMethod(ceh.getMethod());
        lfh.setDosTime(ceh.getDosTime());
        lfh.setCrc(ceh.getCrc());
        lfh.setCompressedSize(ceh.getCompressedSize());
        lfh.setSize(ceh.getSize());
        lfh.setFileName(ceh.getFileName());
        lfh.updateDataDescriptor();
        return lfh;
    }

    public static LocalFileHeader read(InputStream inputStream) throws IOException {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        localFileHeader.readBytes(inputStream);
        if(localFileHeader.isValidSignature()){
            return localFileHeader;
        }
        return null;
    }
    private static final int OFFSET_signature = 0;
    private static final int OFFSET_versionMadeBy = 4;
    private static final int OFFSET_platform = 5;
    private static final int OFFSET_general_purpose = 6;
    private static final int OFFSET_method = 8;
    private static final int OFFSET_dos_time = 10;
    private static final int OFFSET_crc = 14;
    private static final int OFFSET_compressed_size = 18;
    private static final int OFFSET_size = 22;
    private static final int OFFSET_fileNameLength = 26;
    private static final int OFFSET_extraLength = 28;

    private static final int OFFSET_fileName = 30;

}

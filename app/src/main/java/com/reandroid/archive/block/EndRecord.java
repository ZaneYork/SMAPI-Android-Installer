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

import com.reandroid.archive.ArchiveException;
import com.reandroid.archive.ZipSignature;
import com.reandroid.archive.io.ZipInput;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.io.InputStream;

public class EndRecord extends ZipHeader{
    private Zip64Locator zip64Locator;
    private Zip64Record zip64Record;
    public EndRecord() {
        super(MIN_LENGTH, ZipSignature.END_RECORD);
    }

    public int getTotalBytesCount(){
        int count = countBytes();
        ZipHeader zipHeader = getZip64Locator();
        if(zipHeader != null){
            count += zip64Locator.countBytes();
        }
        zipHeader = getZip64Record();
        if(zipHeader != null){
            count += zip64Locator.countBytes();
        }
        return count;
    }
    private boolean isZip64Value(){
        return getInteger(OFFSET_offsetOfCentralDirectory) == 0xffffffff;
    }
    public Zip64Locator getZip64Locator(){
        return zip64Locator;
    }
    public void setZip64Locator(Zip64Locator zip64Locator){
        this.zip64Locator = zip64Locator;
    }
    public Zip64Record getZip64Record(){
        return zip64Record;
    }
    public void setZip64Record(Zip64Record zip64Record){
        this.zip64Record = zip64Record;
    }
    public void findEndRecord(ZipInput zipInput) throws IOException {
        byte[] footer = zipInput.getFooter(SignatureFooter.MIN_SIZE + EndRecord.MAX_LENGTH);
        findEndRecord(footer);
        Zip64Locator zip64Locator = getZip64Locator();
        if(zip64Locator == null){
            return;
        }
        Zip64Record zip64Record = new Zip64Record();
        InputStream inputStream = zipInput.getInputStream(zip64Locator.getOffsetZip64Record(),
                Zip64Record.MAX_LENGTH);
        zip64Record.readBytes(inputStream);
        if(!zip64Record.isValidSignature()){
            throw new IOException("Invalid " + ZipSignature.ZIP64_RECORD + ": "
                    + HexUtil.toHex8(zip64Record.getSignatureValue()));
        }
        setZip64Record(zip64Record);
    }
    public void findEndRecord(byte[] footer) throws IOException {
        int length = footer.length;
        int minLength = EndRecord.MIN_LENGTH;
        int start = length - minLength;
        int offset = 0;
        for(offset = start; offset >= 0; offset--){
            putBytes(footer, offset, 0, minLength);
            if(isValidSignature()){
                break;
            }
        }
        if(!isValidSignature()){
            throw new ArchiveException("Failed to find end record");
        }
        if(!isZip64Value()){
            return;
        }
        Zip64Locator zip64Locator = new Zip64Locator();
        minLength = Zip64Locator.MIN_LENGTH;
        offset = offset - minLength;
        while (offset >= 0){
            zip64Locator.putBytes(footer, offset, 0, minLength);
            if(zip64Locator.isValidSignature()){
                break;
            }
            offset--;
        }
        if(!zip64Locator.isValidSignature()){
            throw new ArchiveException("Failed to find zip64 locator");
        }
        setZip64Locator(zip64Locator);
    }


    public int getNumberOfDisk(){
        return getShortUnsigned(OFFSET_numberOfDisk);
    }
    public void setNumberOfDisk(int value){
        putShort(OFFSET_numberOfDisk, value);
    }
    public int getCentralDirectoryStartDisk(){
        return getShortUnsigned(OFFSET_centralDirectoryStartDisk);
    }
    public void setCentralDirectoryStartDisk(int value){
        putShort(OFFSET_centralDirectoryStartDisk, value);
    }
    public int getNumberOfDirectories(){
        return getShortUnsigned(OFFSET_numberOfDirectories);
    }
    public void setNumberOfDirectories(int value){
        putShort(OFFSET_numberOfDirectories, value);
        Zip64Record zip64Record = getZip64Record();
        if(zip64Record != null){
            zip64Record.setNumberOfCDRecords(value);
        }
    }
    public int getTotalNumberOfDirectories(){
        return getShortUnsigned(OFFSET_totalNumberOfDirectories);
    }
    public void setTotalNumberOfDirectories(int value){
        putShort(OFFSET_totalNumberOfDirectories, value);
        Zip64Record zip64Record = getZip64Record();
        if(zip64Record != null){
            zip64Record.setTotalCDRecords(value);
        }
    }
    public long getLengthOfCentralDirectory(){
        return getIntegerUnsigned(OFFSET_lengthOfCentralDirectory);
    }
    public void setLengthOfCentralDirectory(long value){
        putInteger(OFFSET_lengthOfCentralDirectory, value);
        Zip64Record zip64Record = getZip64Record();
        if(zip64Record != null){
            zip64Record.setSizeOfCD(value);
        }
    }
    public long getOffsetOfCentralDirectory(){
        Zip64Record zip64Record = getZip64Record();
        if(zip64Record != null){
            return zip64Record.getOffsetOfCentralDirectory();
        }
        return getIntegerUnsigned(OFFSET_offsetOfCentralDirectory);
    }
    public void setOffsetOfCentralDirectory(long value){
        if((value & 0xffffffff00000000L) == 0){
            putInteger(OFFSET_offsetOfCentralDirectory, value);
            this.zip64Locator = null;
            this.zip64Record = null;
        }else {
            Zip64Record zip64Record = this.zip64Record;
            if(zip64Record == null){
                zip64Record = Zip64Record.newZip64Record();
                this.zip64Record = zip64Record;
            }
            putInteger(OFFSET_offsetOfCentralDirectory, -1);
            zip64Record.setOffsetOfCentralDirectory(value);
            Zip64Locator zip64Locator = this.zip64Locator;
            if(zip64Locator == null){
                zip64Locator = Zip64Locator.newZip64Locator();
                this.zip64Locator = zip64Locator;
            }
        }
    }
    public int getLastShort(){
        return getShortUnsigned(OFFSET_lastShort);
    }
    public void getLastShort(int value){
        putShort(OFFSET_lastShort, value);
    }


    @Override
    public String toString(){
        if(countBytes()<getMinByteLength()){
            return "Invalid";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getSignature());
        if(isZip64Value()){
            builder.append(", ZIP64");
        }
        builder.append(", disks=").append(getNumberOfDisk());
        builder.append(", start disk=").append(getCentralDirectoryStartDisk());
        builder.append(", dirs=").append(getNumberOfDirectories());
        builder.append(", total dirs=").append(getTotalNumberOfDirectories());
        builder.append(", length=").append(getLengthOfCentralDirectory());
        builder.append(", offset=").append(getOffsetOfCentralDirectory());
        builder.append(", last=").append(HexUtil.toHex8(getLastShort()));
        return builder.toString();
    }

    private static final int OFFSET_numberOfDisk = 4;
    private static final int OFFSET_centralDirectoryStartDisk = 6;
    private static final int OFFSET_numberOfDirectories = 8;
    private static final int OFFSET_totalNumberOfDirectories = 10;
    private static final int OFFSET_lengthOfCentralDirectory = 12;
    private static final int OFFSET_offsetOfCentralDirectory = 16;
    private static final int OFFSET_lastShort = 20;

    public static final int MIN_LENGTH = 22;
    public static final int MAX_LENGTH = 0xffff + 22;
}

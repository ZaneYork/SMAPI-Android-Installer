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

public class Zip64Record  extends ZipHeader{
    public Zip64Record() {
        super(MIN_LENGTH, ZipSignature.ZIP64_RECORD);
    }

    public long getSizeOfEOCDR(){
        return getLong(OFFSET_sizeOfEOCDR);
    }
    public void setSizeOfEOCDR(long value){
        putLong(OFFSET_sizeOfEOCDR, value);
    }
    public int getVersionCreator(){
        return getShortUnsigned(OFFSET_versionCreator);
    }
    public void setVersionCreator(int value){
        putShort(OFFSET_versionCreator, value);
    }
    public int getVersionViewer(){
        return getShortUnsigned(OFFSET_versionCreator);
    }
    public void setVersionViewer(int value){
        putShort(OFFSET_versionViewer, value);
    }
    public int getDiskNumber(){
        return getInteger(OFFSET_diskNumber);
    }
    public void setDiskNumber(int value){
        putInteger(OFFSET_diskNumber, value);
    }
    public int getDiskCD(){
        return getInteger(OFFSET_diskCD);
    }
    public void setDiskCD(int value){
        putInteger(OFFSET_diskCD, value);
    }
    public long getNumberOfCDRecords(){
        return getLong(OFFSET_numberOfCDRecords);
    }
    public void setNumberOfCDRecords(long value){
        putLong(OFFSET_numberOfCDRecords, value);
    }
    public long getTotalCDRecords(){
        return getLong(OFFSET_totalCDRecords);
    }
    public void setTotalCDRecords(long value){
        putLong(OFFSET_totalCDRecords, value);
    }
    public long getSizeOfCD(){
        return getLong(OFFSET_sizeOfCD);
    }
    public void setSizeOfCD(long value){
        putLong(OFFSET_sizeOfCD, value);
    }
    public long getOffsetOfCentralDirectory(){
        return getLong(OFFSET_offsetOfCentralDirectory);
    }
    public void setOffsetOfCentralDirectory(long value){
        putLong(OFFSET_offsetOfCentralDirectory, value);
    }

    @Override
    public String toString(){
        if(countBytes() < getMinByteLength()){
            return "Invalid";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getSignature());
        builder.append(", EOCDR=").append(getSizeOfEOCDR());
        builder.append(", creator=").append(getVersionCreator());
        builder.append(", viewer=").append(getVersionViewer());
        builder.append(", disk number=").append(getDiskNumber());
        builder.append(", disk CD=").append(getDiskCD());
        builder.append(", noOf CDR=").append(getNumberOfCDRecords());
        builder.append(", total rec=").append(getTotalCDRecords());
        builder.append(", size of CD=").append(getSizeOfCD());
        builder.append(", offset of CD=").append(getOffsetOfCentralDirectory());
        return builder.toString();
    }
    public static Zip64Record newZip64Record(){
        Zip64Record zip64Record = new Zip64Record();
        zip64Record.setSignature(ZipSignature.ZIP64_RECORD);
        zip64Record.setSizeOfEOCDR(44);
        zip64Record.setVersionCreator(45);
        zip64Record.setVersionViewer(45);
        return zip64Record;
    }
    public static final int MIN_LENGTH = 56;
    public static final int MAX_LENGTH = 56;


    private static final int OFFSET_sizeOfEOCDR = 4;
    private static final int OFFSET_versionCreator = 12;
    private static final int OFFSET_versionViewer = 14;
    private static final int OFFSET_diskNumber = 16;
    private static final int OFFSET_diskCD = 20;
    private static final int OFFSET_numberOfCDRecords = 24;
    private static final int OFFSET_totalCDRecords = 32;
    private static final int OFFSET_sizeOfCD = 40;
    private static final int OFFSET_offsetOfCentralDirectory = 48;

}

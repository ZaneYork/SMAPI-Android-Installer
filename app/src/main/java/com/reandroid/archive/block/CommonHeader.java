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

import com.reandroid.archive.Archive;
import com.reandroid.archive.ZipSignature;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class CommonHeader extends ZipHeader {
    private final int offsetFileName;
    private final int offsetGeneralPurpose;
    private final GeneralPurposeFlag generalPurposeFlag;
    private String mFileName;
    private long mFileOffset;
    public CommonHeader(int offsetFileName, ZipSignature expectedSignature, int offsetGeneralPurpose){
        super(offsetFileName, expectedSignature);
        this.offsetFileName = offsetFileName;
        this.offsetGeneralPurpose = offsetGeneralPurpose;
        this.generalPurposeFlag = new GeneralPurposeFlag(this, offsetGeneralPurpose);
        this.generalPurposeFlag.setUtf8(true, false);
        setDosTime(0x2210821);
    }
    public long getFileOffset() {
        return mFileOffset;
    }
    public void setFileOffset(long fileOffset){
        this.mFileOffset = fileOffset;
    }
    public long getDataSize(){
        if(getMethod() == Archive.STORED){
            return getSize();
        }
        return getCompressedSize();
    }
    public void setDataSize(long size){
        if(getMethod() == Archive.STORED){
            setSize(size);
        }
        setCompressedSize(size);
    }

    @Override
    int readNext(InputStream inputStream) throws IOException {
        int read = 0;
        read += readFileName(inputStream);
        read += readExtra(inputStream);
        read += readComment(inputStream);
        mFileName = null;
        return read;
    }
    private int readFileName(InputStream inputStream) throws IOException {
        int fileNameLength = getFileNameLength();
        if(fileNameLength==0){
            mFileName = "";
            return 0;
        }
        setFileNameLength(fileNameLength);
        byte[] bytes = getBytesInternal();
        int read = inputStream.read(bytes, offsetFileName, fileNameLength);
        if(read != fileNameLength){
            throw new IOException("Stream ended before reading file name: read="
                    +read+", name length="+fileNameLength);
        }
        mFileName = null;
        return fileNameLength;
    }
    private int readExtra(InputStream inputStream) throws IOException {
        int extraLength = getExtraLength();
        if(extraLength==0){
            return 0;
        }
        setExtraLength(extraLength);
        byte[] bytes = getBytesInternal();
        int offset = getOffsetExtra();
        int read = inputStream.read(bytes, offset, extraLength);
        if(read != extraLength){
            throw new IOException("Stream ended before reading extra bytes: read="
                    + read +", extra length="+extraLength);
        }
        return extraLength;
    }
    int readComment(InputStream inputStream) throws IOException {
        return 0;
    }
    public int getVersionMadeBy(){
        return getShortUnsigned(OFFSET_versionMadeBy);
    }
    public void setVersionMadeBy(int value){
        putShort(OFFSET_versionMadeBy, value);
    }
    public void setVersionExtract(int value){

    }
    public int getPlatform(){
        return getByteUnsigned(OFFSET_platform);
    }
    public void setPlatform(int value){
        getBytesInternal()[OFFSET_platform] = (byte) value;
    }
    public GeneralPurposeFlag getGeneralPurposeFlag() {
        return generalPurposeFlag;
    }
    public int getMethod(){
        return getShortUnsigned(offsetGeneralPurpose + 2);
    }
    public void setMethod(int value){
        putShort(offsetGeneralPurpose + 2, value);
    }
    public long getDosTime(){
        return getIntegerUnsigned(offsetGeneralPurpose + 4);
    }
    public void setDosTime(long value){
        if(value != -1){
            putInteger(offsetGeneralPurpose + 4, value);
        }
    }
    public Date getDate(){
        return Archive.dosToJavaDate(getDosTime());
    }
    public void setDate(Date date){
        setDosTime(Archive.javaToDosTime(date));
    }
    public void setDate(long date){
        setDosTime(Archive.javaToDosTime(date));
    }
    public long getCrc(){
        return getIntegerUnsigned(offsetGeneralPurpose + 8);
    }
    public void setCrc(long value){
        putInteger(offsetGeneralPurpose + 8, value);
    }
    public long getCompressedSize(){
        return getIntegerUnsigned(getOffsetCompressedSize());
    }
    public void setCompressedSize(long value){
        putInteger(getOffsetCompressedSize(), value);
    }
    int getOffsetCompressedSize(){
        return offsetGeneralPurpose + 12;
    }
    public long getSize(){
        return getIntegerUnsigned(getOffsetSize());
    }
    public void setSize(long value){
        putInteger(getOffsetSize(), value);
    }
    int getOffsetSize(){
        return offsetGeneralPurpose + 16;
    }
    public int getFileNameLength(){
        return getShortUnsigned(offsetGeneralPurpose + 20);
    }
    private void setFileNameLength(int value){
        int length = offsetFileName + value + getExtraLength() + getCommentLength();
        super.setBytesLength(length, false);
        putShort(offsetGeneralPurpose + 20, value);
    }
    public int getExtraLength(){
        return getShortUnsigned(offsetGeneralPurpose + 22);
    }
    public void setExtraLength(int value){
        int length = offsetFileName + getFileNameLength() + value + getCommentLength();
        super.setBytesLength(length, false);
        putShort(offsetGeneralPurpose + 22, value);
    }
    public void setZipAlign(int value){
        int length = value;
        if(isZip64()){
            length = getZip64BytesLength() + value;
        }
        setExtraLength(length);
    }
    public byte[] getExtra(){
        int length = getExtraLength();
        byte[] result = new byte[length];
        if(length==0){
            return result;
        }
        byte[] bytes = getBytesInternal();
        int offset = getOffsetExtra();
        System.arraycopy(bytes, offset, result, 0, length);
        return result;
    }
    public void setExtra(byte[] extra){
        if(extra == null){
            extra = new byte[0];
        }
        int length = extra.length;
        setExtraLength(length);
        if(length == 0){
            return;
        }
        putBytes(extra, 0, getOffsetExtra(), length);
    }
    public int getCommentLength(){
        return 0;
    }


    void ensureZip64(){
        if(getExtraLength() >= getZip64BytesLength()){
            return;
        }
        setExtraLength(getZip64BytesLength());
    }

    boolean isZip64(){
        return isZip64Value() && getExtraLength() >= getZip64BytesLength();
    }
    boolean isZip64Value(){
        return isZip64Value(getInteger(getOffsetCompressedSize()))
                || isZip64Value(getInteger(getOffsetCompressedSize()));
    }

    public long getZip64CompressedSize(){
        return getLong(getOffsetZip64CompressedSize());
    }
    public void setZip64CompressedSize(long value){
        putLong(getOffsetZip64CompressedSize(), value);
    }
    public long getZip64Size(){
        return getLong(getOffsetZip64Size());
    }
    void setZip64Size(long value){
        putLong(getOffsetZip64Size(), value);
    }
    int getZip64FieldLength(){
        return getShortUnsigned(getOffsetZip64FieldLength());
    }
    void setZip64FieldLength(int value){
        putShort(getOffsetZip64FieldLength(), value);
    }
    int getZip64FieldHeader(){
        return getShortUnsigned(getOffsetZip64FieldHeader());
    }
    void setZip64FieldHeader(int value){
        putShort(getOffsetZip64FieldHeader(), value);
    }


    int getOffsetZip64CompressedSize(){
        return getOffsetExtra() + 8;
    }
    private int getOffsetZip64Size(){
        return getOffsetZip64FieldLength() + 2;
    }
    int getOffsetZip64FieldLength(){
        return getOffsetZip64FieldHeader() + 2;
    }
    private int getOffsetZip64FieldHeader(){
        return getOffsetZip64();
    }
    private int getOffsetZip64(){
        return getOffsetExtra();
    }
    int getZip64BytesLength(){
        return 20;
    }
    int getOffsetComment(){
        return getOffsetExtra() + getExtraLength();
    }
    private int getOffsetExtra(){
        return offsetFileName + getFileNameLength();
    }

    public String getFileName(){
        if(mFileName == null){
            mFileName = decodeFileName();
        }
        return mFileName;
    }
    public void setFileName(String fileName){
        if(fileName==null){
            fileName="";
        }
        byte[] nameBytes = fileName.getBytes();
        getGeneralPurposeFlag().setUtf8(true, false);
        int length = nameBytes.length;
        setFileNameLength(length);
        if(length==0){
            mFileName = fileName;
            return;
        }
        byte[] bytes = getBytesInternal();
        System.arraycopy(nameBytes, 0, bytes, offsetFileName, length);
        mFileName = fileName;
    }
    public boolean isUtf8(){
        return getGeneralPurposeFlag().getUtf8();
    }
    public boolean hasDataDescriptor() {
        return getGeneralPurposeFlag().hasDataDescriptor();
    }
    public void setHasDataDescriptor(boolean hasDataDescriptor) {
        getGeneralPurposeFlag().setHasDataDescriptor(hasDataDescriptor);
    }
    private String decodeFileName(){
        int length = getFileNameLength();
        byte[] bytes = getBytesInternal();
        int offset = offsetFileName;
        int max = bytes.length - offset;
        if(max<=0){
            return "";
        }
        if(length>max){
            length = max;
        }
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }
    public String decodeComment(){
        int length = getExtraLength();
        byte[] bytes = getBytesInternal();
        int offset = getOffsetExtra();
        int max = bytes.length - offset;
        if(max<=0){
            return "";
        }
        if(length>max){
            length = max;
        }
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }
    void onUtf8Changed(boolean oldValue){
        String str = mFileName;
        if(str != null){
            setFileName(str);
        }
    }

    @Override
    public String toString(){
        if(countBytes()<getMinByteLength()){
            return "Invalid";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[').append(getFileOffset()).append("] ");
        String str = getFileName();
        boolean appendOnce = false;
        if(str.length()>0){
            builder.append("name=").append(str);
            appendOnce = true;
        }
        if(appendOnce){
            builder.append(", ");
        }
        builder.append("SIG=").append(getSignature());
        builder.append(", versionMadeBy=").append(HexUtil.toHex4((short) getVersionMadeBy()));
        builder.append(", platform=").append(HexUtil.toHex2((byte) getPlatform()));
        builder.append(", GP={").append(getGeneralPurposeFlag()).append("}");
        builder.append(", method=").append(getMethod());
        builder.append(", date=").append(HexUtil.toHex(getDosTime(), 1));
        builder.append(", crc=").append(HexUtil.toHex8(getCrc()));
        builder.append(", cSize=").append(getCompressedSize());
        builder.append(", size=").append(getSize());
        builder.append(", fileNameLength=").append(getFileNameLength());
        builder.append(", extraLength=").append(getExtraLength());
        return builder.toString();
    }

    static boolean isZip64Value(long value){
        return value == 0xffffffffL || (value & 0xffffffff00000000L) != 0;
    }
    static boolean isZip64Value(int value){
        return value == 0xffffffff;
    }

    public static class GeneralPurposeFlag {
        private final CommonHeader localFileHeader;
        private final int offset;
        public GeneralPurposeFlag(CommonHeader commonHeader, int offset){
            this.localFileHeader = commonHeader;
            this.offset = offset;
        }

        public boolean getEncryption(){
            return this.localFileHeader.getBit(offset, 0);
        }
        public void setEncryption(boolean flag){
            this.localFileHeader.putBit(offset, 0, flag);
        }
        public boolean hasDataDescriptor(){
            return this.localFileHeader.getBit(offset, 3);
        }
        public void setHasDataDescriptor(boolean flag){
            this.localFileHeader.putBit(offset, 3, flag);
        }
        public boolean getStrongEncryption(){
            return this.localFileHeader.getBit(offset, 6);
        }
        public void setStrongEncryption(boolean flag){
            this.localFileHeader.putBit(offset, 6, flag);
        }
        public boolean getUtf8(){
            return this.localFileHeader.getBit(offset + 1, 3);
        }
        public void setUtf8(boolean flag){
            setUtf8(flag, true);
        }
        private void setUtf8(boolean flag, boolean notify){
            boolean oldUtf8 = getUtf8();
            if(oldUtf8 == flag){
                return;
            }
            this.localFileHeader.putBit(offset +1, 3, flag);
            if(notify){
                this.localFileHeader.onUtf8Changed(oldUtf8);
            }
        }

        public int getValue(){
            return this.localFileHeader.getShortUnsigned(offset);
        }
        public void setValue(int value){
            if(value == getValue()){
                return;
            }
            boolean oldUtf8 = getUtf8();
            this.localFileHeader.putShort(offset, value);
            if(oldUtf8 != getUtf8()){
                this.localFileHeader.onUtf8Changed(oldUtf8);
            }
        }
        public void initDefault(){
            setUtf8(false, false);
        }

        @Override
        public String toString(){
            return "Enc="+ getEncryption()
                    +", Descriptor="+ hasDataDescriptor()
                    +", StrongEnc="+ getStrongEncryption()
                    +", UTF8="+ getUtf8();
        }
    }

    private static final int OFFSET_versionMadeBy = 4;
    private static final int OFFSET_platform = 5;

}

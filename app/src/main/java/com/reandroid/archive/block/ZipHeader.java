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

public class ZipHeader extends ZipBlock{
    private final ZipSignature expectedSignature;
    private final int minByteLength;
    public ZipHeader(int minByteLength, ZipSignature expectedSignature) {
        super(minByteLength);
        this.minByteLength = minByteLength;
        this.expectedSignature = expectedSignature;
    }
    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        int read = readBasic(inputStream);
        ZipSignature sig=getSignature();
        if(sig != getExpectedSignature()){
            return read;
        }
        read += readNext(inputStream);
        return read;
    }
    private int readBasic(InputStream inputStream) throws IOException {
        setBytesLength(getMinByteLength(), false);
        byte[] bytes = getBytesInternal();
        int beginLength = bytes.length;
        int read = inputStream.read(bytes, 0, beginLength);
        if(read != beginLength){
            setBytesLength(read, false);
            if(getSignature()==expectedSignature){
                setSignature(0);
            }
            return read;
        }
        return read;
    }
    int readNext(InputStream inputStream) throws IOException {
        return 0;
    }
    public boolean isValidSignature(){
        return getSignature() == getExpectedSignature();
    }
    ZipSignature getExpectedSignature(){
        return expectedSignature;
    }
    int getMinByteLength() {
        return minByteLength;
    }

    public ZipSignature getSignature(){
        return ZipSignature.valueOf(getSignatureValue());
    }
    public int getSignatureValue(){
        if(countBytes()<4){
            return 0;
        }
        return getInteger(OFFSET_signature);
    }
    public void setSignature(int value){
        if(countBytes()<4){
            return;
        }
        putInteger(OFFSET_signature, value);
    }
    public void setSignature(ZipSignature signature){
        setSignature(signature == null ? 0:signature.getValue());
    }

    public static boolean isZip64Length(long length){
        return (length == 0xffffffff || (length & 0xffffffff00000000L) != 0);
    }
    private static final int OFFSET_signature = 0;
}

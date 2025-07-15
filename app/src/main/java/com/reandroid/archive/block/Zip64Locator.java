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

public class Zip64Locator extends ZipHeader{
    public Zip64Locator() {
        super(MIN_LENGTH, ZipSignature.ZIP64_LOCATOR);
    }

    public long getOffsetZip64Record(){
        return getLong(OFFSET_offsetZip64Record);
    }
    public void setOffsetZip64Record(long value){
        putLong(OFFSET_offsetZip64Record, value);
    }
    public int getNumberOfDisks(){
        return getInteger(OFFSET_numberOfDisks);
    }
    public void setNumberOfDisks(int value){
        putInteger(OFFSET_numberOfDisks, value);
    }

    @Override
    public String toString(){
        if(countBytes() < getMinByteLength()){
            return "Invalid";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getSignature());
        builder.append(", offsetZip64Record=").append(getOffsetZip64Record());
        builder.append(", numberOfDisks=").append(getNumberOfDisks());
        return builder.toString();
    }
    public static Zip64Locator newZip64Locator(){
        Zip64Locator zip64Locator = new Zip64Locator();
        zip64Locator.setSignature(ZipSignature.ZIP64_LOCATOR);
        zip64Locator.setNumberOfDisks(1);
        return zip64Locator;
    }
    private static final int OFFSET_offsetZip64Record = 8;
    private static final int OFFSET_numberOfDisks = 16;

    public static final int MIN_LENGTH = 20;
}

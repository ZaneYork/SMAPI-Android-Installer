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

public enum ZipSignature {
    CENTRAL_FILE(0X02014B50),
    LOCAL_FILE(0X04034B50),
    DATA_DESCRIPTOR(0X08074B50),
    ZIP64_RECORD(0X06064B50),
    ZIP64_LOCATOR(0X07064B50),
    END_RECORD(0X06054B50);

    private final int value;

    ZipSignature(int value){
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    public static ZipSignature valueOf(int value){
        for(ZipSignature signature:VALUES){
            if(value == signature.getValue()){
                return signature;
            }
        }
        return null;
    }
    private static final ZipSignature[] VALUES = values();
}

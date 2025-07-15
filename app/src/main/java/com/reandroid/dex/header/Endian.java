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
package com.reandroid.dex.header;

public class Endian extends HeaderPiece{
    public Endian(){
        super(12);
        putInteger(0, LITTLE);
    }

    public int getType(){
        return getInteger(0);
    }
    public void setType(int type){
        setSize(12);
        putInteger(0, type);
        putInteger(4, 0);
        putInteger(8, 0);
    }
    public boolean isBig(){
        return getType() == BIG;
    }
    public boolean isLittle(){
        return getType() == LITTLE;
    }
    @Override
    public String toString(){
        if(isBig()){
            return "BIG";
        }
        if(isLittle()){
            return "LITTLE";
        }
        return printHex(getBytesInternal());
    }

    public static final int BIG = 0x78563412;
    public static final int LITTLE = 0x12345678;
}

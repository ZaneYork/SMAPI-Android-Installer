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

import com.reandroid.arsc.base.Block;
import com.reandroid.utils.HexUtil;

import java.util.zip.Adler32;

public class Checksum extends HeaderPiece {

    public Checksum(){
        super(4);
    }

    public int getValue(){
        return getInteger(0);
    }
    public void setValue(long checksum){
        setSize(4);
        putInteger(0, (int)checksum);
    }
    public void update(Block parent, byte[] bytes) {
        int start = parent.countUpTo(this) + countBytes();
        Adler32 adler32 = new Adler32();
        adler32.update(bytes, start, bytes.length - start);
        setValue(adler32.getValue());
    }
    @Override
    public String toString(){
        return HexUtil.toHex8(getValue());
    }
}

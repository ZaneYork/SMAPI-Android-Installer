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
import com.reandroid.utils.SHA1;

public class Signature extends HeaderPiece {

    public Signature(){
        super(20);
    }

    public void update(Block parent, byte[] bytes) {
        SHA1 sha1 = new SHA1();
        int start = parent.countUpTo(this) + countBytes();
        sha1.update(bytes, start, bytes.length - start);
        sha1.digest(getBytesInternal());
    }
    public String getHex() {
        return HexUtil.toHexString(getBytesInternal());
    }
    @Override
    public String toString() {
        return getHex();
    }
}

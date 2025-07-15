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

import com.reandroid.arsc.item.ByteArray;

public class Magic extends HeaderPiece {
    public Magic(){
        super();
        super.set(DEFAULT_BYTES.clone());
    }
    public void resetDefault(){
        super.set(DEFAULT_BYTES.clone());
    }
    public boolean isDefault(){
        return ByteArray.equals(getBytesInternal(), DEFAULT_BYTES);
    }

    public static final byte[] DEFAULT_BYTES = new byte[]{(byte)'d', (byte)'e', (byte)'x', (byte)0x0A};

}

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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;

// General purpose block to consume the remaining bytes of BlockReader
public class BottomBlock extends BlockList<LengthPrefixedBytes> {
    public BottomBlock(){
        super();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        while (reader.isAvailable()){
            LengthPrefixedBytes prefixedBytes = new LengthPrefixedBytes(false);
            prefixedBytes.readBytes(reader);
            this.add(prefixedBytes);
        }
    }
}

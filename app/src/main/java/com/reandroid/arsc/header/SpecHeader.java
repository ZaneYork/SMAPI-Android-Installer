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
package com.reandroid.arsc.header;

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;

import java.io.IOException;

 public class SpecHeader extends HeaderBlock{
    private final ByteItem id;
    private final IntegerItem entryCount;
    public SpecHeader() {
        super(ChunkType.SPEC.ID);
        this.id = new ByteItem();
        ByteItem res0 = new ByteItem();
        ShortItem res1 = new ShortItem();
        this.entryCount = new IntegerItem();
        addChild(id);
        addChild(res0);
        addChild(res1);
        addChild(entryCount);
    }
    public ByteItem getId() {
        return id;
    }
    public IntegerItem getEntryCount() {
        return entryCount;
    }
    @Override
    public String toString(){
        if(getChunkType() != ChunkType.SPEC){
            return super.toString();
        }
        return getClass().getSimpleName()
                +" {id="+getId().toHex()
                +", entryCount=" + getEntryCount() + '}';
    }

     public static SpecHeader read(BlockReader reader) throws IOException {
         SpecHeader specHeader = new SpecHeader();
         if(reader.available() < specHeader.countBytes()){
             throw new IOException("Too few bytes to read spec header, available = " + reader.available());
         }
         int pos = reader.getPosition();
         specHeader.readBytes(reader);
         reader.seek(pos);
         return specHeader;
     }
}

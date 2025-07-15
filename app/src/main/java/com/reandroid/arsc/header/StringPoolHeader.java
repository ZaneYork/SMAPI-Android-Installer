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
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;


public class StringPoolHeader extends HeaderBlock{
    private final IntegerItem countStrings;
    private final IntegerItem countStyles;
    private final ByteItem flagSorted;
    private final ByteItem flagUtf8;
    private final ShortItem flagExtra;
    private final IntegerItem startStrings;
    private final IntegerItem startStyles;
    public StringPoolHeader() {
        super(ChunkType.STRING.ID);
        this.countStrings = new IntegerItem();
        this.countStyles = new IntegerItem();
        this.flagSorted = new ByteItem();
        this.flagUtf8 = new ByteItem();
        this.flagExtra = new ShortItem();
        this.startStrings = new IntegerItem();
        this.startStyles = new IntegerItem();

        addChild(countStrings);
        addChild(countStyles);
        addChild(flagSorted);
        addChild(flagUtf8);
        addChild(flagExtra);
        addChild(startStrings);
        addChild(startStyles);
    }
    public IntegerItem getCountStrings() {
        return countStrings;
    }
    public IntegerItem getCountStyles() {
        return countStyles;
    }
    public ByteItem getFlagUtf8() {
        return flagUtf8;
    }
    public ByteItem getFlagSorted() {
        return flagSorted;
    }
    public ShortItem getFlagExtra(){
        return flagExtra;
    }
    public IntegerItem getStartStrings() {
        return startStrings;
    }
    public IntegerItem getStartStyles() {
        return startStyles;
    }

    public boolean isUtf8(){
        return (getFlagUtf8().getByte() & 0x01) !=0;
    }
    public void setUtf8(boolean utf8){
        getFlagUtf8().set((byte) (utf8 ? 0x01 : 0x00));
    }
    public boolean isSorted(){
        return (getFlagSorted().getByte() & 0x01) !=0;
    }
    public void setSorted(boolean sorted){
        getFlagSorted().set((byte) (sorted ? 0x01 : 0x00));
    }

    @Override
    public String toString(){
        if(getChunkType()!=ChunkType.STRING){
            return super.toString();
        }
        return getClass().getSimpleName()
                +" {strings="+getCountStrings()
                +", styles="+getCountStyles()
                +", utf8="+isUtf8()
                +", sorted="+isSorted()
                +", flagExtra="+getFlagExtra().toHex()
                +", offset-strings="+getStartStrings().get()
                +", offset-styles="+getStartStyles().get() + '}';
    }
}

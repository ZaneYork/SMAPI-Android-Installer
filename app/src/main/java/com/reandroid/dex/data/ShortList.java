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
package com.reandroid.dex.data;

import com.reandroid.arsc.item.ShortArrayBlock;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.PositionAlignedItem;
import com.reandroid.utils.HexUtil;

public class ShortList extends IntegerList implements PositionAlignedItem {
    public ShortList(){
        super(0, new ShortArrayBlock(), new DexPositionAlign());
    }

    @Override
    public void put(int index, int value){
        if((value & 0xffff0000) != 0){
            throw new DexException("Short value out of range "
                    + HexUtil.toHex(value, 4) + " > 0xffff");
        }
        super.put(index, value);
    }
    @Override
    public String toString() {
        DexPositionAlign dexPositionAlign = getPositionAlign();
        if(dexPositionAlign.size() > 0){
            return super.toString() + dexPositionAlign;
        }
        return super.toString();
    }
}

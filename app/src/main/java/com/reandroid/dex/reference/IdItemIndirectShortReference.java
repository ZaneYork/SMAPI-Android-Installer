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
package com.reandroid.dex.reference;

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.HexUtil;

public class IdItemIndirectShortReference<T extends IdItem> extends IdItemIndirectReference<T> {

    public IdItemIndirectShortReference(SectionType<T> sectionType, SectionItem blockItem, int offset, int usage) {
        super(sectionType, blockItem, offset, usage);
        Block.putShort(getBytesInternal(), getOffset(), 0xffff);
    }

    @Override
    public int get() {
        return Block.getShortUnsigned(getBytesInternal(), getOffset());
    }
    @Override
    public void set(int value) {
        if((value & 0xffff0000) != 0){
            throw new DexException("Short value out of range "
                    + HexUtil.toHex(value, 4) + " > 0xffff");
        }
        Block.putShort(getBytesInternal(), getOffset(), value);
    }

    @Override
    protected T pullItem(int i) {
        if(i == 0xffff){
            return null;
        }
        return super.pullItem(i);
    }
}

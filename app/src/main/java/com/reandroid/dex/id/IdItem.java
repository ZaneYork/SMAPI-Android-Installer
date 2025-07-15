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
package com.reandroid.dex.id;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.EditableItem;
import com.reandroid.dex.common.IdUsageIterator;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.ModifiableKeyItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;

import java.io.IOException;
import java.util.Iterator;

public abstract class IdItem extends SectionItem
        implements SmaliFormat, BlockRefresh, EditableItem,
        ModifiableKeyItem, FixedSizeBlock, IdUsageIterator {

    IdItem(int bytesLength) {
        super(bytesLength);
    }

    @Override
    public abstract Key getKey();
    @Override
    public abstract void setKey(Key key);
    public abstract SectionType<? extends IdItem> getSectionType();
    @Override
    public abstract Iterator<IdItem> usedIds();
    @SuppressWarnings("unchecked")
    @Override
    public void removeSelf(){
        BlockListArray<IdItem> itemArray = getParentInstance(BlockListArray.class);
        if(itemArray != null){
            itemArray.remove(this);
            setParent(null);
            setIndex(-1);
        }
    }
    abstract void cacheItems();

    @Override
    public int getIdx(){
        return getIndex();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        cacheItems();
    }
}

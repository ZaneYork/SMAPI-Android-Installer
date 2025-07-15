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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.OffsetReceiver;
import com.reandroid.dex.base.PositionedItem;
import com.reandroid.dex.common.IdUsageIterator;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.common.SectionItemContainer;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.key.ModifiableKeyItem;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class DataItem extends SectionItemContainer
        implements PositionedItem, OffsetSupplier, OffsetReceiver, KeyItem, IdUsageIterator {

    private Block mUniqueUser;
    private boolean mShared;

    public DataItem(int childesCount) {
        super(childesCount);
    }

    @Override
    public Iterator<IdItem> usedIds() {
        return EmptyIterator.of();
    }

    @Override
    public void setReplace(SectionItem replace) {
        super.setReplace(replace);
        replace = getReplace();
        if(replace != null && replace != this){
            ((DataItem)replace).addUniqueUser(mUniqueUser);
        }
    }
    public void copyFrom(DataItem item){
        if(item == null){
            return;
        }
        if(this instanceof ModifiableKeyItem){
            ModifiableKeyItem self = (ModifiableKeyItem) this;
            self.setKey(((ModifiableKeyItem)item).getKey());
            return;
        }
        BlockReader reader = new BlockReader(item.getBytes());
        try {
            this.readBytes(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public boolean isSharedItem() {
        return mShared;
    }
    public boolean isSharedItem(Block uniqueUser) {
        if(this.isSharedItem()){
            return true;
        }
        Block user = this.mUniqueUser;
        return user != null && uniqueUser != null && user != uniqueUser;
    }
    public void addUniqueUser(Block uniqueUser){
        if(uniqueUser == null || mShared || uniqueUser == mUniqueUser || uniqueUser == this){
            return;
        }
        if(mUniqueUser != null){
            mShared = true;
        }else {
            mUniqueUser = uniqueUser;
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    public void removeSelf() {
        Block parent = getParent();
        if(parent == null){
            return;
        }
        BlockList<DataItem> itemArray = (BlockList<DataItem>)parent;
        itemArray.remove(this);
        setPosition(0);
        mUniqueUser = null;
    }
    @Override
    public Key getKey() {
        return null;
    }

    public void removeLastAlign(){

    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        if(mUniqueUser != null && mUniqueUser.getParent() == null){
            mUniqueUser = null;
        }
    }
}

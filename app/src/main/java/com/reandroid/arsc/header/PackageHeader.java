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
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.arsc.item.IntegerItem;

public class PackageHeader extends HeaderBlock{
    private final IntegerItem packageId;
    private final FixedLengthString packageName;

    private final IntegerItem typeStringPoolOffset;
    private final IntegerItem typeStringPoolCount;
    private final IntegerItem specStringPoolOffset;
    private final IntegerItem specStringPoolCount;
    private final SingleBlockContainer<IntegerItem> typeIdOffsetContainer;
    private final IntegerItem typeIdOffset;

    public PackageHeader() {
        super(ChunkType.PACKAGE.ID);
        this.packageId = new IntegerItem();
        this.packageName = new FixedLengthString(256);

        this.typeStringPoolOffset = new IntegerItem();
        this.typeStringPoolCount = new IntegerItem();
        this.specStringPoolOffset = new IntegerItem();
        this.specStringPoolCount = new IntegerItem();

        this.typeIdOffsetContainer = new SingleBlockContainer<>();
        this.typeIdOffset = new IntegerItem();
        this.typeIdOffsetContainer.setItem(typeIdOffset);

        addChild(this.packageId);
        addChild(this.packageName);
        addChild(this.typeStringPoolOffset);
        addChild(this.typeStringPoolCount);
        addChild(this.specStringPoolOffset);
        addChild(this.specStringPoolCount);
        addChild(this.typeIdOffsetContainer);
    }

    public IntegerItem getPackageId() {
        return packageId;
    }
    public FixedLengthString getPackageName() {
        return packageName;
    }
    public IntegerItem getTypeStringPoolOffset() {
        return typeStringPoolOffset;
    }
    public IntegerItem getTypeStringPoolCount() {
        return typeStringPoolCount;
    }
    public IntegerItem getSpecStringPoolOffset() {
        return specStringPoolOffset;
    }
    public IntegerItem getSpecStringPoolCount() {
        return specStringPoolCount;
    }
    public IntegerItem getTypeIdOffsetItem() {
        return typeIdOffset;
    }
    public void setTypeIdOffset(int offset){
        typeIdOffset.set(offset);
        typeIdOffsetContainer.setItem(typeIdOffset);
    }
    public int getTypeIdOffset() {
        if(typeIdOffset.getParent()==null){
            typeIdOffset.set(0);
        }
        return typeIdOffset.get();
    }
    @Override
    void onHeaderSizeLoaded(int size){
        super.onHeaderSizeLoaded(size);
        if(size<288){
            typeIdOffset.set(0);
            typeIdOffsetContainer.setItem(null);
        }
    }
}

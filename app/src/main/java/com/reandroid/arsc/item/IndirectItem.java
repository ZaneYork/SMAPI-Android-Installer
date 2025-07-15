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
package com.reandroid.arsc.item;

import java.util.Objects;

public class IndirectItem<T extends BlockItem> {
    private final T blockItem;
    private final int offset;
    public IndirectItem(T blockItem, int offset){
        this.blockItem = blockItem;
        this.offset = offset;
    }
    public byte[] getBytesInternal(){
        return blockItem.getBytesInternal();
    }
    public T getBlockItem() {
        return blockItem;
    }
    public int getOffset() {
        return offset;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IndirectItem<?> other = (IndirectItem<?>) obj;
        return this.getOffset() == other.getOffset() && this.getBlockItem() == other.getBlockItem();
    }
    @Override
    public int hashCode(){
        return Objects.hash(this.getOffset(), this.getBlockItem());
    }
}

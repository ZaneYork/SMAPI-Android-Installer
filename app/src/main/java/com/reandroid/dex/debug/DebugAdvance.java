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
package com.reandroid.dex.debug;

import com.reandroid.dex.base.Le128;

public abstract class DebugAdvance extends DebugElement{

    private final Le128 advance;

    DebugAdvance(DebugElementType<?> elementType, Le128 advance) {
        super(1, elementType);
        this.advance = advance;
        addChild(1, advance);
    }
    public int getAdvance(){
        return advance.get();
    }
    public void setAdvance(int advance){
        this.advance.set(advance);
    }
    @Override
    public boolean isValid() {
        return !isRemoved() && getAdvance() != 0;
    }

    @Override
    public void merge(DebugElement element){
        super.merge(element);
        DebugAdvance coming = (DebugAdvance) element;
        this.advance.set(coming.advance.get());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugAdvance debugAdvance = (DebugAdvance) obj;
        return getFlag() == debugAdvance.getFlag() &&
                advance.get() == debugAdvance.advance.get();
    }
    @Override
    public int hashCode() {
        int hash = getFlag();
        hash = hash * 31 + advance.get();
        return hash;
    }
    @Override
    public String toString() {
        return "advance=" + advance;
    }
}

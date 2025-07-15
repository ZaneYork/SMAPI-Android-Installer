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
package com.reandroid.dex.base;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.item.IntegerReference;

public class ParallelReference implements IntegerReference, BlockRefresh {

    private final IntegerReference reference1;
    private IntegerReference reference2;

    public ParallelReference(IntegerReference reference1, IntegerReference reference2){
        this.reference1 = reference1;
        this.reference2 = reference2;
    }
    public ParallelReference(IntegerReference reference1){
        this(reference1, null);
    }

    public IntegerReference getReference2() {
        return reference2;
    }
    public void setReference2(IntegerReference reference2) {
        if(reference2 != this){
            this.reference2 = reference2;
        }
    }

    @Override
    public void set(int value) {
        this.reference1.set(value);
        if(reference2 != null){
            reference2.set(value);
        }
    }
    @Override
    public int get() {
        return reference1.get();
    }
    @Override
    public void refresh() {
        set(get());
    }
    public int get2() {
        IntegerReference ref2 = this.reference2;
        if(ref2 != null){
            return ref2.get();
        }
        return reference1.get();
    }

    @Override
    public String toString() {
        int i1 = reference1.get();
        if(reference2 == null){
            return Integer.toString(i1);
        }
        int i2 = reference2.get();
        if(i1 == i2){
            return Integer.toString(i1);
        }
        return "reference1=" + reference1 + ", reference2=" + reference2;
    }
}

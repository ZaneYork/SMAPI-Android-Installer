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

import com.reandroid.arsc.item.IntegerReference;

public class WarpedIntegerReference implements IntegerReference {
    private IntegerReference reference;
    public WarpedIntegerReference(IntegerReference reference){
        this.reference = reference;
    }
    public WarpedIntegerReference(){
        this(null);
    }
    public IntegerReference getReference() {
        return reference;
    }
    public void setReference(IntegerReference reference) {
        if(reference != this){
            this.reference = reference;
        }
    }

    @Override
    public void set(int value) {
        if(reference != null){
            reference.set(value);
        }
    }
    @Override
    public int get() {
        if(reference != null){
            return reference.get();
        }
        return -1;
    }
    @Override
    public String toString() {
        if(reference == null){
            return "NULL";
        }
        return Integer.toString(get());
    }
}

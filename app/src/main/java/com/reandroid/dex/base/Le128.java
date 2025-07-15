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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;

public abstract class Le128 extends DexBlockItem implements IntegerReference {

    private int value;

    public Le128() {
        super(1);
    }

    @Override
    public int get() {
        return value;
    }
    @Override
    public void set(int value) {
        if(value == this.value){
            return;
        }
        this.value = value;
        writeValue(value);
    }
    protected abstract void writeValue(int value);
    protected abstract int readLe128(BlockReader reader) throws IOException;
    public void onReadBytes(BlockReader reader) throws IOException {
        this.value = readLe128(reader);
    }

    @Override
    public String toString() {
        return Integer.toString(get());
    }
}

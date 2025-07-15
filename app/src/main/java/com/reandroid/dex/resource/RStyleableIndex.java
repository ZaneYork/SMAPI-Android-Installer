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
package com.reandroid.dex.resource;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.model.DexField;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class RStyleableIndex extends RStyleableItem implements IntegerReference {

    public RStyleableIndex(DexField dexField) {
        super(dexField);
    }

    @Override
    public int get() {
        IntegerReference reference = getDexField().getStaticIntegerValue();
        if(reference != null) {
            return reference.get();
        }
        return 0;
    }
    @Override
    public void set(int value) {
        IntegerReference reference = getDexField().getStaticIntegerValue();
        if(reference != null) {
            reference.set(value);
        }
    }
    @Override
    public void appendJavaValue(SmaliWriter writer) throws IOException {
        writer.appendHex(get());
    }

    @Override
    public boolean isValid() {
        if(!super.isValid()) {
            return false;
        }
        return (get() & 0xffff0000) == 0;
    }
}

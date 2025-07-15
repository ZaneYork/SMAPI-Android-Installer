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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.common.HiddenApiFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class HiddenApiFlagValue extends Ule128Item
        implements SmaliFormat, Comparable<HiddenApiFlagValue> {

    private Def<?> def;

    public HiddenApiFlagValue(){
        super();
        set(HiddenApiFlag.NO_RESTRICTION);
    }

    HiddenApiFlagValue newCopy(){
        return new Copy(this);
    }
    public void linkDef(Def<?> def) {
        this.def = def;
        def.setHiddenApiFlagValue(this);
    }

    public Iterator<HiddenApiFlag> getFlags(){
        return HiddenApiFlag.valuesOf(get());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendModifiers(getFlags());
    }

    @Override
    public int compareTo(HiddenApiFlagValue hiddenApiFlagValue) {
        if(hiddenApiFlagValue == null){
            return -1;
        }
        if(hiddenApiFlagValue == this){
            return 0;
        }
        return SectionTool.compareIndex(def, hiddenApiFlagValue.def);
    }


    @Override
    public int hashCode() {
        return get();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HiddenApiFlagValue)) {
            return false;
        }
        HiddenApiFlagValue flagValue = (HiddenApiFlagValue) obj;
        return get() == flagValue.get();
    }

    @Override
    public String toString() {
        return Modifier.toString(getFlags());
    }

    static class Copy extends HiddenApiFlagValue {
        private final HiddenApiFlagValue source;

        Copy(HiddenApiFlagValue source){
            super();
            this.source = source;
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
        }
        @Override
        public int countBytes() {
            return 0;
        }
        @Override
        public int get() {
            return source.get();
        }
        @Override
        public void set(int value) {
            source.set(value);
        }
        @Override
        protected void writeValue(int value) {
        }
        @Override
        protected int onWriteBytes(OutputStream stream) throws IOException {
            return 0;
        }
        @Override
        public boolean isNull() {
            return true;
        }
        @Override
        public byte[] getBytes() {
            return null;
        }
    }

}

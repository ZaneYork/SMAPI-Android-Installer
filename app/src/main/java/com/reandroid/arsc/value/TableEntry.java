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
package com.reandroid.arsc.value;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public abstract class TableEntry<HEADER extends ValueHeader, VALUE extends Block> extends Block implements
        JSONConvert<JSONObject> {
    private final HEADER header;
    private final VALUE resValue;

    TableEntry(HEADER header, VALUE resValue){
        super();
        this.header = header;
        this.resValue = resValue;

        this.header.setParent(this);
        this.header.setIndex(0);
        this.resValue.setParent(this);
        this.resValue.setIndex(1);
    }
    public abstract Iterator<ValueItem> allValues();
    public Entry getParentEntry(){
        return getParent(Entry.class);
    }
    public void refresh(){
    }
    public final HEADER getHeader() {
        return header;
    }
    public VALUE getValue(){
        return resValue;
    }

    @Override
    public byte[] getBytes() {
        byte[] results = getHeader().getBytes();
        results = addBytes(results, getValue().getBytes());
        return results;
    }
    @Override
    public int countBytes() {
        int result = getHeader().countBytes();
        result += getValue().countBytes();
        return result;
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        getHeader().onCountUpTo(counter);
        getValue().onCountUpTo(counter);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HEADER header = getHeader();
        header.readBytes(reader);
        VALUE value = getValue();
        onHeaderLoaded(value, header);
        value.readBytes(reader);
    }

    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        int result;
        result = getHeader().writeBytes(stream);
        result += getValue().writeBytes(stream);
        return result;
    }

    void onHeaderLoaded(VALUE value, HEADER header){
    }
    abstract void onRemoved();
    abstract boolean canMerge(TableEntry<?, ?> tableEntry);
    abstract void linkTableStringsInternal(TableStringPool tableStringPool);

    public abstract void merge(TableEntry<?, ?> tableEntry);
    public abstract void mergeWithName(ResourceMergeOption mergeOption, TableEntry<?, ?> tableEntry);
    @Override
    public abstract JSONObject toJson();
    @Override
    public abstract void fromJson(JSONObject json);
    @Override
    public String toString(){
        return getHeader()+", value={"+getValue()+"}";
    }
}

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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.debug.DebugElement;
import com.reandroid.dex.debug.DebugSequence;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.DataKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.ModifiableKeyItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class DebugInfo extends DataItem implements ModifiableKeyItem {

    private final Ule128Item lineStart;
    private final Ule128Item debugParameterCount;
    private BlockList<DebugParameter> debugParametersArray;
    private final DebugSequence debugSequence;

    private final DataKey<DebugInfo> debugKey;
    public DebugInfo() {
        super(4);
        this.lineStart = new Ule128Item(true);
        this.debugParameterCount = new Ule128Item();

        this.debugSequence = new DebugSequence(lineStart);

        addChild(0, lineStart);
        addChild(1, debugParameterCount);
        // index = 2, debugParametersArray
        addChild(3, debugSequence);

        this.debugKey = new DataKey<>(this);
    }

    public void removeInvalidElements(){
        DebugSequence debugSequence = getDebugSequence();
        if(debugSequence != null){
            debugSequence.removeInvalid();
        }
    }
    @Override
    public DataKey<DebugInfo> getKey() {
        return debugKey;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key){
        DataKey<DebugInfo> debugKey = (DataKey<DebugInfo>) key;
        merge(debugKey.getItem());
    }
    @Override
    public SectionType<DebugInfo> getSectionType() {
        return SectionType.DEBUG_INFO;
    }

    public int getParameterCount(){
        if(debugParametersArray != null){
            return debugParametersArray.size();
        }
        return 0;
    }
    public DebugParameter getDebugParameter(int index){
        BlockList<DebugParameter> debugParametersArray = this.debugParametersArray;
        if(debugParametersArray != null){
            return debugParametersArray.get(index);
        }
        return null;
    }
    public DebugParameter getOrCreateDebugParameter(int index){
        BlockList<DebugParameter> debugParametersArray = initParametersArray();
        debugParametersArray.ensureSize(index + 1);
        return debugParametersArray.get(index);
    }
    public void removeDebugParameter(int index){
        BlockList<DebugParameter> parameterNames = this.debugParametersArray;
        if(parameterNames == null){
            return;
        }
        DebugParameter parameter = parameterNames.get(index);
        if(parameter == null){
            return;
        }
        if(index == parameterNames.getCount() - 1){
            parameterNames.remove(parameter);
        }else {
            parameter.set(0);
        }
        parameterNames.refresh();
    }
    public Iterator<DebugParameter> getParameters(){
        if(debugParametersArray != null){
            return debugParametersArray.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<DebugElement> getExtraLines(){
        return getDebugSequence().getExtraLines();
    }
    public DebugSequence getDebugSequence(){
        return debugSequence;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        lineStart.onReadBytes(reader);
        debugParameterCount.onReadBytes(reader);
        if(debugParameterCount.get() > 0){
            BlockList<DebugParameter> array = initParametersArray();
            array.setSize(debugParameterCount.get());
            array.readChildes(reader);
        }
        debugSequence.onReadBytes(reader);
    }
    private BlockList<DebugParameter> initParametersArray(){
        BlockList<DebugParameter> debugParametersArray = this.debugParametersArray;
        if(debugParametersArray == null){
            debugParametersArray = new BlockList<>();
            this.debugParametersArray = debugParametersArray;
            addChild(2, debugParametersArray);
            debugParametersArray.setCreator(CREATOR);
        }
        return debugParametersArray;
    }

    public void onRemove(){
        BlockList<DebugParameter> array = this.debugParametersArray;
        if(array != null){
            this.debugParametersArray = null;
            array.clearChildes();
        }
        this.debugSequence.clear();
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        removeInvalidElements();
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        int count = 0;
        if(debugParametersArray != null){
            count = debugParametersArray.size();
        }
        debugParameterCount.set(count);
    }

    public Iterator<IdItem> usedIds(){
        Iterator<IdItem> iterator1 = new IterableIterator<DebugParameter, IdItem>(getParameters()) {
            @Override
            public Iterator<IdItem> iterator(DebugParameter element) {
                return element.usedIds();
            }
        };
        return CombiningIterator.two(iterator1, getDebugSequence().usedIds());
    }
    public void merge(DebugInfo debugInfo){
        if(debugInfo == this){
            return;
        }
        lineStart.set(debugInfo.lineStart.get());
        int count = debugInfo.getParameterCount();
        debugParameterCount.set(count);
        if(count != 0){
            BlockList<DebugParameter> array = initParametersArray();
            array.setSize(count);
            for(int i = 0; i < count; i++){
                DebugParameter comingParameter = debugInfo.getDebugParameter(i);
                DebugParameter parameter = array.get(i);
                parameter.merge(comingParameter);
            }
        }
        getDebugSequence().merge(debugInfo.getDebugSequence());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugInfo debugInfo = (DebugInfo) obj;
        return lineStart.get() == lineStart.get() &&
                Objects.equals(debugParametersArray, debugInfo.debugParametersArray)&&
                Objects.equals(debugSequence, debugInfo.debugSequence);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash*31 + lineStart.get();
        BlockList<DebugParameter> array = this.debugParametersArray;
        hash = hash * 31;
        if(array != null){
            hash = hash + array.hashCode();
        }
        hash = hash * 31;
        hash = hash + debugSequence.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "DebugInfo{" +
                "lineStart=" + lineStart.get() +
                ", parameterCount=" + debugParameterCount.get() +
                ", sequence=(" + debugSequence +
                ")}";
    }

    private static final Creator<DebugParameter> CREATOR = new Creator<DebugParameter>() {
        @Override
        public DebugParameter[] newArrayInstance(int length) {
            if(length == 0){
                return EMPTY;
            }
            return new DebugParameter[length];
        }
        @Override
        public DebugParameter newInstance() {
            return new DebugParameter();
        }
    };
    static final DebugParameter[] EMPTY = new DebugParameter[0];

}

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
package com.reandroid.dex.reference;

import com.reandroid.dex.base.DexException;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.data.CodeItem;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.ins.SizeXIns;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.HexUtil;

import java.util.Objects;

public class InsIdSectionReference implements IdReference<IdItem> {

    private final SizeXIns sizeXIns;
    private IdItem item;

    public InsIdSectionReference(SizeXIns sizeXIns){
        this.sizeXIns = sizeXIns;
    }

    @Override
    public IdItem getItem() {
        return item;
    }
    @Override
    public void setItem(IdItem item) {
        item = validateReplace(item);
        this.item = item;
        set(item.getIdx());
        updateUsage();
    }
    @Override
    public Key getKey() {
        return getItem().getKey();
    }
    @Override
    public void setItem(Key key) {
        IdItem item = sizeXIns.getOrCreateSection(getSectionType())
                .getOrCreate(key);
        setItem(item);
    }
    @Override
    public void pullItem() {
        setItem(sizeXIns.getSectionItem(getSectionType(), get()));
    }
    @Override
    public void refresh() {
        IdItem item = validateReplace(this.item);
        this.item = item;
        set(item.getIdx());
        updateUsage();
    }
    public void validate() {
        validateReplace(this.item);
    }
    private void updateUsage(){
        IdItem item = this.item;
        if(item != null){
            item.addUsageType(UsageMarker.USAGE_INSTRUCTION);
        }
    }
    @Override
    public int get() {
        return sizeXIns.getData();
    }
    @Override
    public void set(int value) {
        sizeXIns.setData(value);
    }
    @SuppressWarnings("unchecked")
    @Override
    public SectionType<IdItem> getSectionType() {
        return (SectionType<IdItem>) sizeXIns.getSectionType();
    }

    private IdItem validateReplace(IdItem idItem){
        if(idItem == null){
            throw new DexException("null id item: " + buildTrace(null));
        }
        idItem = idItem.getReplace();
        if(idItem == null){
            throw new DexException("Invalid id item: " + buildTrace(null));
        }
        return validateType(idItem);
    }
    private IdItem validateType(IdItem idItem){
        Key key = idItem.getKey();
        if(key instanceof TypeKey){
            TypeKey typeKey = (TypeKey) key;
            if(this.item != null && !typeKey.isTypeObject() && !sizeXIns.is(Opcode.CONST_CLASS)){
                throw new DexException("Unexpected type '" + key + "', " + buildTrace(idItem));
            }
        }
        return idItem;
    }
    private String buildTrace(IdItem item){
        SizeXIns sizeXIns = this.sizeXIns;
        InstructionList instructionList = sizeXIns.getInstructionList();
        if(instructionList == null){
            return "removed instruction";
        }
        CodeItem codeItem = instructionList.getCodeItem();
        if(codeItem == null){
            return "removed instruction list";
        }
        if(codeItem.getParent() == null){
            return "removed code item";
        }
        MethodDef methodDef = codeItem.getMethodDef();
        StringBuilder builder = new StringBuilder();
        if(methodDef != null){
            builder.append("method = ");
            builder.append(methodDef.getKey());
            builder.append(", ");
        }
        builder.append(sizeXIns.getOpcode());
        String key = toDebugString(item);
        if(key == null){
            key = HexUtil.toHex(get(), 1);
        }
        builder.append(", key = '");
        builder.append(key);
        builder.append('\'');
        return builder.toString();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InsIdSectionReference sectionItem = (InsIdSectionReference) obj;
        return Objects.equals(getItem(), sectionItem.getItem());
    }
    @Override
    public int hashCode() {
        IdItem idItem = getItem();
        if(idItem == null){
            return 0;
        }
        return idItem.hashCode();
    }
    @Override
    public String toString() {
        IdItem item = this.item;
        if(item == null){
            return getSectionType().getName() + ": " + get();
        }
        return item.getKey().toString();
    }
    private static String toDebugString(IdItem item) {
        if(item == null){
            return null;
        }
        Key key = item.getKey();
        if(key == null){
            return null;
        }
        String keyString = key.toString();
        if(keyString == null){
            return null;
        }
        if(keyString.length() > 100){
            keyString = keyString.substring(0, 100) + "...";
        }
        if(keyString.startsWith("\"")){
            keyString = keyString.substring(1);
        }
        if(keyString.endsWith("\"")){
            keyString = keyString.substring(0, keyString.length() - 1);
        }
        return keyString;
    }
}

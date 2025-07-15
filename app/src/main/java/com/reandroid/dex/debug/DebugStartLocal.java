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

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.Base1Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliDebug;
import com.reandroid.dex.smali.model.SmaliDebugElement;
import com.reandroid.dex.smali.model.SmaliDebugLocal;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class DebugStartLocal extends DebugRegisterNumber {

    private final Base1Ule128IdItemReference<StringId> mName;
    private final Base1Ule128IdItemReference<TypeId> mType;

    DebugStartLocal(int childesCount, int flag) {
        super(childesCount + 2, flag);

        this.mName = new Base1Ule128IdItemReference<>(SectionType.STRING_ID);
        this.mType = new Base1Ule128IdItemReference<>(SectionType.TYPE_ID);

        addChild(2, mName);
        addChild(3, mType);
    }
    DebugStartLocal(int childesCount, DebugElementType<?> elementType) {
        this(childesCount, elementType.getFlag());
    }
    public DebugStartLocal() {
        this(0, DebugElementType.START_LOCAL.getFlag());
    }

    @Override
    public boolean isValid(){
        return !isRemoved() && mName.getItem() != null && mType.getItem() != null;
    }
    public String getName(){
        StringId stringId = mName.getItem();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public StringKey getNameKey(){
        return (StringKey) this.mName.getKey();
    }
    public void setName(String name){
        this.setName(StringKey.create(name));
    }
    public void setName(StringKey key){
        this.mName.setItem(key);
    }
    public String getType(){
        TypeId typeId = mType.getItem();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public TypeId getTypeId(){
        return mType.getItem();
    }
    public TypeKey getTypeKey(){
        return (TypeKey) mType.getKey();
    }
    public void setType(String type){
        this.mType.setItem(TypeKey.create(type));
    }
    public void setType(TypeKey typeKey){
        this.mType.setItem(typeKey);
    }

    @Override
    public void appendExtra(SmaliWriter writer) throws IOException {
        if(isValid()) {
            getSmaliDirective().append(writer);
            writer.appendRegister(getRegisterNumber());
            writer.append(", ");
            this.mName.append(writer);
            writer.append(':');
            this.mType.append(writer);
        }
    }

    @Override
    public DebugElementType<? extends DebugStartLocal> getElementType() {
        return DebugElementType.START_LOCAL;
    }


    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.two(SingleIterator.of(mName.getItem()),
                SingleIterator.of(mType.getItem()));
    }
    @Override
    public void merge(DebugElement element){
        super.merge(element);
        DebugStartLocal coming = (DebugStartLocal) element;
        this.mName.setItem(coming.mName.getKey());
        this.mType.setItem(coming.mType.getKey());
    }

    @Override
    public void fromSmali(SmaliDebugElement smaliDebugElement) throws IOException {
        super.fromSmali(smaliDebugElement);
        SmaliDebugLocal smaliDebugLocal = (SmaliDebugLocal) smaliDebugElement;
        setName(smaliDebugLocal.getName());
        setType(smaliDebugLocal.getType());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugStartLocal debug = (DebugStartLocal) obj;
        return getFlag() == debug.getFlag() &&
                Objects.equals(getName(), debug.getName()) &&
                Objects.equals(getType(), debug.getType());
    }
    @Override
    public int hashCode() {
        int hash = getFlag();
        hash = hash * 31;
        String text = getName();
        if(text != null){
            hash = hash + text.hashCode();
        }
        hash = hash * 31;
        text = getType();
        if(text != null){
            hash = hash + text.hashCode();
        }
        return hash;
    }
    @Override
    public String toString() {
        return super.toString() + ", " + mName + ':' + mType;
    }
}

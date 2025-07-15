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

import com.reandroid.dex.data.DebugInfo;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.reference.Base1Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class DebugParameter extends Base1Ule128IdItemReference<StringId> implements SmaliFormat {

    public DebugParameter(){
        super(SectionType.STRING_ID);
    }

    public String getName(){
        StringId stringId = getNameId();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public void setName(String name){
        if(name == null || name.length() == 0){
            setItem((StringId) null);
        }else {
            setItem(new StringKey(name));
        }
    }
    public StringId getNameId(){
        if(isRemoved()) {
            return null;
        }
        return getItem();
    }
    public boolean isRemoved() {
        DebugInfo debugInfo = getParentInstance(DebugInfo.class);
        if(debugInfo != null) {
            return debugInfo.isRemoved();
        }
        return true;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        StringId stringId = getNameId();
        if(stringId == null){
            return;
        }
        writer.append(", ");
        stringId.append(writer);
    }

    public Iterator<IdItem> usedIds(){
        return SingleIterator.of(getItem());
    }
    public void merge(DebugParameter parameter){
        setItem(parameter.getKey());
    }

    @Override
    public String toString() {
        if(getItem() == null){
            return super.toString();
        }
        return  ".param p **" + ", \"" + getNameId() + "\"";
    }
}

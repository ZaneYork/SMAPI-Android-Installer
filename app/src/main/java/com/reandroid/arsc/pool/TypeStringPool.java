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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.array.TypeStringArray;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.utils.collection.CollectionUtil;


public class TypeStringPool extends StringPool<TypeString> {

    private final IntegerItem mTypeIdOffset;

    public TypeStringPool(boolean is_utf8, IntegerItem typeIdOffset) {
        super(is_utf8, false);
        this.mTypeIdOffset = typeIdOffset;
    }
    public int getLastId(){
        int count = size();
        return toTypeId(count - 1);
    }
    public int idOf(String typeName){
        return idOf(getByName(typeName));
    }
    /**
     * Resolves id of {@link TypeBlock}
     * Not recommend to use unless unless you are sure of proper pool
     **/
    public int idOf(TypeString typeString){
        if(typeString == null){
            return 0;
        }
        return (toTypeId(typeString.getIndex()));
    }
    /**
     * Searches string entry {@link TypeBlock}
     * {@param name} is name of {@link TypeBlock}
     * This might not working if duplicate type names are present
     **/
    public TypeString getByName(String name){
        for(TypeString typeString : this){
            if(name.equals(typeString.get())){
                return typeString;
            }
        }
        return null;
    }
    public TypeString getById(int id){
        return super.get(toIndex(id));
    }
    public TypeString getOrCreate(int typeId, String typeName){
        StringArray<TypeString> stringsArray = getStringsArray();
        int size = toIndex(typeId) + 1;
        stringsArray.ensureSize(size);
        TypeString typeString = getById(typeId);
        typeString.set(typeName);
        return typeString;
    }
    private int toIndex(int typeId){
        return typeId - 1 - mTypeIdOffset.get();
    }
    private int toTypeId(int index){
        return index + 1 + mTypeIdOffset.get();
    }
    /**
     * Use getOrCreate(typeId, typeName)}
     **/
    @Deprecated
    @Override
    public final TypeString getOrCreate(String str){
        TypeString typeString = CollectionUtil.getSingle(getAll(str));
        if(typeString == null){
            throw new IllegalArgumentException("Can not create TypeString (" + str
                    +") without type id. use getOrCreate(typeId, typeName)");
        }
        return typeString;
    }
    @Override
    StringArray<TypeString> newInstance(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new TypeStringArray(offsets, itemCount, itemStart, is_utf8);
    }
}

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
package com.reandroid.arsc.value.plurals;

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.value.AttributeType;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.arsc.value.bag.MapBag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PluralsBag extends MapBag<AttributeType, PluralsBagItem> {
    private PluralsBag(com.reandroid.arsc.value.Entry entry) {
        super(entry);
    }

    @Override
    protected PluralsBagItem createBagItem(ResValueMap valueMap, boolean copied) {
        if (copied) {
            return PluralsBagItem.copyOf(valueMap);
        }
        return PluralsBagItem.create(valueMap);
    }

    @Override
    protected ResValueMap newKey(AttributeType key) {
        ResValueMap valueMap;
        ResValueMapArray mapArray = getMapArray();
        if(mapArray != null){
            valueMap = mapArray.createNext();
        }else {
            valueMap = new ResValueMap();
        }
        valueMap.setAttributeType(key);
        return valueMap;
    }

    @Override
    protected AttributeType getKeyFor(ResValueMap valueMap) {
        if(valueMap == null){
            return null;
        }
        AttributeType attributeType = valueMap.getAttributeType();
        if(attributeType != null && attributeType.isPlural()){
            return attributeType;
        }
        return null;
    }

    public String getQuantityString(AttributeType quantity, ResConfig resConfig) {
        PluralsBagItem item = get(quantity);
        if (item == null) {
            return null;
        }
        return item.getQualityString(resConfig);
    }
    public String getQuantityString(AttributeType quantity) {
        return getQuantityString(quantity, null);
    }

    public void setQuantityString(AttributeType quantity, String str) {
        if (quantity == null || str == null) {
            return;
        }
        put(quantity, PluralsBagItem.string(getStringPool().getOrCreate(str)));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        String type = getTypeName();
        builder.append(type);
        builder.append(" name=\"");
        builder.append(getName());
        builder.append("\">");
        for (PluralsBagItem pluralsBagItem : values()) {
            builder.append("\n    ");
            builder.append(pluralsBagItem.toString());
        }
        builder.append("\n</");
        builder.append(type);
        builder.append(">");
        return builder.toString();
    }

    private final static Set<ValueType> validTypes = new HashSet<>(Arrays.asList(ValueType.NULL, ValueType.STRING, ValueType.REFERENCE));

    /**
     * The result of this is not always 100% accurate,
     * in addition to this use your methods to cross check like type-name == "plurals"
     **/
    public static boolean isPlurals(com.reandroid.arsc.value.Entry entry) {
        PluralsBag plurals = create(entry);
        if (plurals == null) {
            return false;
        }
        ResValueMapArray array = plurals.getMapArray();
        if (array.size() == 0) {
            return false;
        }
        Iterator<ResValueMap> iterator = array.iterator();
        while (iterator.hasNext()) {
            ResValueMap item = iterator.next();
            if (item == null || !validTypes.contains(item.getValueType())) {
                return false;
            }
            AttributeType attributeType = item.getAttributeType();
            if(attributeType == null || !attributeType.isPlural()){
                return false;
            }
        }
        return true;
    }

    public static PluralsBag create(com.reandroid.arsc.value.Entry entry) {
        if (entry == null || !entry.isComplex()) {
            return null;
        }
        return new PluralsBag(entry);
    }
}

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

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.*;
import com.reandroid.arsc.value.bag.BagItem;

import java.util.List;

public class PluralsBagItem extends BagItem {
    private PluralsBagItem(ResValueMap bagItem) {
        super(bagItem);
    }

    private PluralsBagItem(StringItem str) {
        super(str);
    }

    private PluralsBagItem(ValueType valueType, int data) {
        super(valueType, data);
    }

    public AttributeType getQuantity() {
        if (mBagItem == null) {
            return null;
        }
        AttributeType attributeType = mBagItem.getAttributeType();
        if(attributeType != null && attributeType.isPlural()){
            return attributeType;
        }
        return null;
    }

    public String getQualityString(ResConfig resConfig) {
        switch (getValueType()) {
            case STRING:
                return getStringValue();
            case REFERENCE:
                Entry entry = null;
                if (mBagItem != null) {
                    entry = mBagItem.getEntry();
                }
                if (entry == null) {
                    return null;
                }

                if (resConfig == null) {
                    resConfig = entry.getResConfig();
                }

                Entry stringRes = null;
                if (resConfig != null) {
                    TableBlock tableBlock = entry.getPackageBlock().getTableBlock();
                    List<Entry> resolvedList = tableBlock.resolveReferenceWithConfig(getValue(), resConfig);
                    if (resolvedList.size() > 0) {
                        stringRes = resolvedList.get(0);
                    }
                }

                if (stringRes == null) {
                    return null;
                }
                ResValue resValue = stringRes.getResValue();
                if (resValue == null || resValue.getValueType() != ValueType.STRING) {
                    throw new IllegalArgumentException("Not a STR reference: " + formattedRefValue());
                }
                return resValue.getValueAsString();
            default:
                throw new IllegalArgumentException("Not STR/REFERENCE ValueType=" + getValueType());
        }
    }

    private String formattedRefValue() {
        return HexUtil.toHex8("@0x", getValue());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<item quantity=\"");
        builder.append(getQuantity());
        builder.append("\">");
        if (hasStringValue()) {
            builder.append(getStringValue());
        } else {
            builder.append(formattedRefValue());
        }
        builder.append("</item>");
        return builder.toString();
    }

    protected static PluralsBagItem create(ResValueMap resValueMap) {
        if (resValueMap == null) {
            return null;
        }
        return new PluralsBagItem(resValueMap);
    }

    protected static PluralsBagItem copyOf(ResValueMap resValueMap) {
        ValueType valueType = resValueMap.getValueType();
        if (valueType == ValueType.STRING) {
            return new PluralsBagItem(resValueMap.getDataAsPoolString());
        } else {
            return new PluralsBagItem(valueType, resValueMap.getData());
        }
    }

    public static PluralsBagItem string(TableString str) {
        if (str == null) {
            return null;
        }
        return new PluralsBagItem(str);
    }

    public static PluralsBagItem reference(int resourceId) {
        return new PluralsBagItem(ValueType.REFERENCE, resourceId);
    }
}

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
package com.reandroid.arsc.value.style;

import com.reandroid.arsc.coder.CommonType;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.arsc.value.attribute.AttributeBagItem;
import com.reandroid.arsc.value.bag.BagItem;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.ValueType;

public class StyleBagItem extends BagItem {
    private StyleBagItem(ResValueMap bagItem) {
        super(bagItem);
    }

    private StyleBagItem(ValueType valueType, int data) {
        super(valueType, data);
    }

    private StyleBagItem(StringItem str) {
        super(str);
    }

    public String getName() {
        if (mBagItem == null) {
            return null;
        }
        return mBagItem.decodeName(true);
    }
    public Entry getAttributeEntry() {
        if (mBagItem == null) {
            return null;
        }
        return mBagItem.resolveName().get();
    }

    public int getNameId() {
        if (mBagItem == null) {
            return 0;
        }
        return mBagItem.getNameId();
    }

    public boolean hasAttributeValue() {
        return getValueType() == ValueType.ATTRIBUTE;
    }
    public boolean hasIntValue() {
        ValueType valueType = getValueType();
        return valueType == ValueType.DEC || valueType == ValueType.HEX;
    }

    public String getValueAsReference() {
        ValueType valueType = getValueType();
        if (valueType != ValueType.REFERENCE && valueType != ValueType.ATTRIBUTE) {
            throw new IllegalArgumentException("Not REF ValueType=" + valueType);
        }
        return getBagItem().decodeValue();
    }
    public String decodeAttributeValue(AttributeBag attr) {
        if (!hasIntValue()) {
            return null;
        }
        return attr.decodeAttributeValue(getValue());
    }
    public AttributeBagItem[] getFlagsOrEnum(AttributeBag attr) {
        if (!hasIntValue()) {
            return null;
        }
        return attr.searchValue(getValue());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<item name=\"");
        String name = getName();
        if (name == null) {
            name = HexUtil.toHex8("@0x", getNameId());
        }
        builder.append(name);
        builder.append("\">");
        if (hasStringValue()) {
            builder.append(getStringValue());
        }
        String val = null;
        if (hasReferenceValue() || hasAttributeValue()) {
            val = getValueAsReference();
        }
        if (val == null) {
            val = HexUtil.toHex8(getValue());
        }
        builder.append(val);
        builder.append("</item>");
        return builder.toString();
    }

    protected static StyleBagItem create(ResValueMap resValueMap) {
        if (resValueMap == null) {
            return null;
        }
        return new StyleBagItem(resValueMap);
    }

    public static StyleBagItem create(ValueType valueType, int value) {
        if (valueType == null || valueType == ValueType.STRING) {
            return null;
        }
        return new StyleBagItem(valueType, value);
    }

    protected static StyleBagItem copyOf(ResValueMap resValueMap) {
        ValueType valueType = resValueMap.getValueType();
        if (valueType == ValueType.STRING) {
            return new StyleBagItem(resValueMap.getDataAsPoolString());
        } else {
            return new StyleBagItem(valueType, resValueMap.getData());
        }
    }

    public static StyleBagItem integer(int n) {
        return new StyleBagItem(ValueType.DEC, n);
    }

    public static StyleBagItem string(TableString str) {
        if (str == null) {
            return null;
        }
        return new StyleBagItem(str);
    }

    public static StyleBagItem reference(int resourceId) {
        return new StyleBagItem(ValueType.REFERENCE, resourceId);
    }
    public static StyleBagItem attribute(int resourceId) {
        return new StyleBagItem(ValueType.ATTRIBUTE, resourceId);
    }
    public static StyleBagItem encoded(EncodeResult encodeResult) {
        if (encodeResult == null) {
            return null;
        }
        return create(encodeResult.valueType, encodeResult.value);
    }
    public static StyleBagItem color(String color) {
        EncodeResult encodeResult = ValueCoder.encode(color, CommonType.COLOR.valueTypes());
        return encoded(encodeResult);
    }
    public static StyleBagItem dimensionOrFraction(String str) {
        EncodeResult encodeResult = ValueCoder.encode(str, ValueType.DIMENSION);
        if(encodeResult == null){
            encodeResult = ValueCoder.encode(str, ValueType.FRACTION);
        }
        return encoded(encodeResult);
    }
    public static StyleBagItem createFloat(float n) {
        return new StyleBagItem(ValueType.FLOAT, Float.floatToIntBits(n));
    }
    public static StyleBagItem enumOrFlag(AttributeBag attr, String valueString) {
        return encoded(attr.encodeEnumOrFlagValue(valueString));
    }
}

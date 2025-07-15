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
package com.reandroid.arsc.value.array;

import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.arsc.value.bag.BagItem;
import com.reandroid.arsc.value.ResValueMap;

public class ArrayBagItem extends BagItem {
    private ArrayBagItem(ResValueMap valueMap) {
        super(valueMap);
    }

    private ArrayBagItem(StringItem str) {
        super(str);
    }

    private ArrayBagItem(ValueType valueType, int value) {
        super(valueType, value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<item>");
        if (hasStringValue()) {
            builder.append(getStringValue());
        } else {
            builder.append(HexUtil.toHex8(getValue()));
        }
        builder.append("</item>");
        return builder.toString();
    }

    protected static ArrayBagItem create(ResValueMap valueMap) {
        if (valueMap == null) {
            return null;
        }
        return new ArrayBagItem(valueMap);
    }

    public static ArrayBagItem create(ValueType valueType, int value) {
        if (valueType == null || valueType == ValueType.STRING) {
            return null;
        }
        return new ArrayBagItem(valueType, value);
    }

    protected static ArrayBagItem copyOf(ResValueMap resValueMap) {
        ValueType valueType = resValueMap.getValueType();
        if (valueType == ValueType.STRING) {
            return new ArrayBagItem(resValueMap.getDataAsPoolString());
        } else {
            return new ArrayBagItem(valueType, resValueMap.getData());
        }
    }

    public static ArrayBagItem encoded(EncodeResult encodeResult) {
        if (encodeResult == null) {
            return null;
        }
        return create(encodeResult.valueType, encodeResult.value);
    }

    public static ArrayBagItem integer(int n) {
        return create(ValueType.DEC, n);
    }

    public static ArrayBagItem string(TableString str) {
        if (str == null) {
            return null;
        }
        return new ArrayBagItem(str);
    }

    public static ArrayBagItem reference(int resourceId) {
        return create(ValueType.REFERENCE, resourceId);
    }
}

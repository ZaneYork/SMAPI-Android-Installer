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

import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.IntegerItem;

class ResConfigValueContainer extends ByteArray {
    private final IntegerItem configSize;

    ResConfigValueContainer(int bytesSize, IntegerItem configSize){
        super(bytesSize);
        this.configSize = configSize;
    }

    int getByteValue(int offset){
        return getValue(offset, 1);
    }
    void setByteValue(int offset, int value){
        setValue(offset, 1, value);
    }
    int getShortValue(int offset){
        return getValue(offset, 2);
    }
    void setShortValue(int offset, int value){
        setValue(offset, 2, value);
    }
    int getIntValue(int offset){
        return getValue(offset, 4);
    }
    long getLongValue(int offset){
        return getLong(getBytesInternal(), offset);
    }
    void setIntValue(int offset, int value){
        setValue(offset, 4, value);
    }
    void setByteArrayValue(int offset, byte[] bytes, int length){
        if(bytes == null || bytes.length == 0){
            if(size() - offset < length){
                return;
            }
        }
        bytes = ensureArrayLength(bytes, length);
        setValue(offset, bytes);
    }
    byte[] getByteArrayValue(int offset, int length){
        int size = size();
        int available = size - offset;
        if(available < length || available <= 0){
            return null;
        }
        return getByteArray(offset, length);
    }

    private int getValue(int offset, int dataSize){
        int size = size();
        if(size < offset + dataSize){
            return 0;
        }
        if(dataSize == 1){
            return getByteUnsigned(offset);
        }
        if(dataSize == 2){
            return getShortUnsigned(offset) & 0xffff;
        }
        if(dataSize == 4){
            return getInteger(offset);
        }
        throw new IllegalArgumentException("Invalid data size " + dataSize);
    }
    private void setValue(int offset, byte[] bytes){
        int size = size();
        int valueSize = offset + bytes.length;
        boolean sizeChanged = false;
        if(size < valueSize){
            if(isNullBytes(bytes)){
                return;
            }
            ensureArraySize(getNearestValueSize(valueSize));
            this.configSize.set(size() + 4);
            sizeChanged = true;
        }
        putByteArray(offset, bytes);
        if(sizeChanged){
            onSizeChanged();
        }
    }
    private void setValue(int offset, int dataSize, int value){
        int size = size();
        int valueSize = offset + dataSize;
        boolean sizeChanged = false;
        if(size < valueSize){
            if(value == 0){
                return;
            }
            ensureArraySize(getNearestValueSize(valueSize));
            this.configSize.set(size() + 4);
            sizeChanged = true;
        }
        boolean valueChanged;
        if(dataSize == 1){
            valueChanged = putByteValue(offset, (byte) value);
        }else if(dataSize == 2){
            valueChanged = putShortValue(offset, value);
        }else if(dataSize == 4){
            valueChanged = putIntegerValue(offset, value);
        }else {
            throw new IllegalArgumentException("Invalid data size " + dataSize);
        }
        if(sizeChanged){
            onSizeChanged();
        }
        if(valueChanged){
            onValueChanged();
        }
    }
    private boolean putByteValue(int offset, byte value){
        if(get(offset) == value){
            return false;
        }
        put(offset, value);
        return true;
    }
    private boolean putShortValue(int offset, int value){
        if(getShortUnsigned(offset) == value){
            return false;
        }
        putShort(offset, value);
        return true;
    }
    private boolean putIntegerValue(int offset, int value){
        if(getInteger(offset) == value){
            return false;
        }
        putInteger(offset, value);
        return true;
    }
    void onSizeChanged(){

    }
    void onValueChanged(){

    }
    private static int getNearestValueSize(int valueSize){
        return ResConfigBase.nearestSize(valueSize + 4) - 4;
    }
    private static byte[] ensureArrayLength(byte[] bytes, int length){
        if(bytes == null || length == 0){
            return new byte[length];
        }
        if(bytes.length == length){
            return bytes;
        }
        byte[] result = new byte[length];
        int max = result.length;
        if(bytes.length < max){
            max = bytes.length;
        }
        System.arraycopy(bytes, 0, result, 0, max);
        return result;
    }
    private static boolean isNullBytes(byte[] bytes){
        if(bytes == null){
            return true;
        }
        for(int i = 0; i < bytes.length; i++){
            if(bytes[i] != 0){
                return false;
            }
        }
        return true;
    }
}

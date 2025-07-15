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
package com.reandroid.common;

import com.reandroid.utils.HexUtil;

public interface IntegerArray {
    int get(int i);
    int size();
    void setSize(int size);
    void put(int i, int value);


    static int[] toArray(IntegerArray array){
        if(isEmpty(array)){
            return EMPTY;
        }
        int size = array.size();
        int[] result = new int[size];
        for(int i = 0; i < size; i++){
            result[i] = array.get(i);
        }
        return result;
    }
    static String toString(IntegerArray array){
        return toString(array, false, 0, 10);
    }
    static String toString(IntegerArray array, boolean hex){
        return toString(array, hex, 0, 10);
    }
    static String toString(IntegerArray array, int start){
        return toString(array, false, start, 10);
    }
    static String toString(IntegerArray array, boolean hex, int start){
        return toString(array, hex, start, 10);
    }
    static String toString(IntegerArray array, boolean hex, int start, int limit){
        if(array == null){
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("size=");
        int size = array.size() - start;
        builder.append(size);
        if(start > 0){
            builder.append(", start=");
            builder.append(start);
        }
        int max = size;
        if(max > limit){
            max = limit;
        }
        for(int i = 0; i < max; i++){
            if(i == 0){
                builder.append(" [");
            }else {
                builder.append(", ");
            }
            int value = array.get(i);
            if(hex){
                builder.append(HexUtil.toHex(value, 2));
            }else {
                builder.append(value);
            }
        }
        if(size > max){
            builder.append(", +");
            builder.append(size - max);
            builder.append(" more");
        }
        if(size > 0){
            builder.append(']');
        }
        return builder.toString();
    }
    static int hashCode(IntegerArray array){
        if(isEmpty(array)){
            return 0;
        }
        int result = 1;
        int size = array.size();
        for(int i = 0; i < size; i++){
            result =  result + array.get(i) * 31;
        }
        return result;
    }
    static boolean equals(IntegerArray array1, IntegerArray array2){
        if(array1 == array2){
            return true;
        }
        if(isEmpty(array1)){
            return isEmpty(array2);
        }
        if(isEmpty(array2)){
            return isEmpty(array1);
        }
        int size = array1.size();
        if(size != array2.size()){
            return false;
        }
        for(int i = 0; i < size; i++){
            if(array1.get(i) != array2.get(i)){
                return false;
            }
        }
        return true;
    }
    static IntegerArray subArray(IntegerArray integerArray, int offset, int size){
        if(isEmpty(integerArray)){
            return integerArray;
        }
        if(offset == 0 && size == integerArray.size()){
            return integerArray;
        }
        return new IntegerArray() {
            @Override
            public int get(int i) {
                return integerArray.get(offset + i);
            }
            @Override
            public int size() {
                return size;
            }
            @Override
            public void setSize(int size) {
            }
            @Override
            public void put(int i, int value) {
            }
            @Override
            public String toString(){
                return IntegerArray.toString(this);
            }
        };
    }
    static boolean isEmpty(int[] array){
        return array == null || array.length == 0;
    }
    static boolean isEmpty(IntegerArray array){
        return array == null || array.size() == 0;
    }

    int[] EMPTY = new int[0];
}

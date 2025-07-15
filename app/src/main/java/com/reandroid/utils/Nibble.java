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
package com.reandroid.utils;

public class Nibble extends Number {

    public static final byte MIN_VALUE = -8;
    public static final byte MAX_VALUE = 7;
    public static final int SIZE = 4;
    public static final int BYTES = 1;

    private static final Nibble[] VALUES;

    private final int value;

    static {
        int length = 16;
        Nibble[] values = new Nibble[16];
        VALUES = values;
        for(int i = 0; i < length; i++){
            values[i] = new Nibble(i - 8);
        }
    }

    public Nibble(int value){
        if(value < -8 || value > 7){
            throw new NumberFormatException("Nibble value out of range: " + value);
        }
        this.value = value;
    }
    @Override
    public int intValue() {
        return value;
    }
    @Override
    public long longValue() {
        return intValue();
    }
    @Override
    public float floatValue() {
        return intValue();
    }
    @Override
    public double doubleValue() {
        return intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Number)) {
            return false;
        }
        Number number = (Number) obj;
        return value == number.intValue();
    }

    @Override
    public int hashCode() {
        return value;
    }
    public String toString() {
        return Integer.toString(intValue());
    }

    public static int toSigned(int unsignedNibble) {
        if(unsignedNibble < 0x8){
            return unsignedNibble;
        }
        return unsignedNibble - 16;
    }
    public static int toUnsigned(int signedNibble) {
        if(signedNibble >= 0){
            return signedNibble;
        }
        return 16 + signedNibble;
    }

    public static Nibble valueOf(int nibble) {
        return VALUES[nibble + 8];
    }
}

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

public class ObjectsUtil {

    public static<T> T getNull() throws ClassCastException {
        return null;
    }
    @SuppressWarnings("unchecked")
    public static<T> T cast(Object obj) throws ClassCastException {
        return (T) obj;
    }
    public static<T> T of(T t) {
        return t;
    }
    public static String of(String str) {
        return str;
    }
    public static byte of(byte b) {
        return b;
    }
    public static short of(short s) {
        return s;
    }
    public static int of(int i) {
        return i;
    }
    public static long of(long l) {
        return l;
    }
    public static float of(float f) {
        return f;
    }
    public static double of(double d) {
        return d;
    }

    public static boolean equals(Object obj1, Object obj2){
        if(obj1 == obj2){
            return true;
        }
        if(obj1 == null || obj2 == null){
            return false;
        }
        return obj1.equals(obj2);
    }
    public static int hash(Object obj){
        if(obj == null){
            return 0;
        }
        return obj.hashCode();
    }
    public static int hash(Object obj1, Object obj2){
        int hash = 1;
        hash = hash * 31 + hash(obj1);
        hash = hash * 31 + hash(obj2);
        return hash;
    }
    public static int hash(Object obj1, Object obj2, Object obj3){
        int hash = 1;
        hash = hash * 31 + hash(obj1);
        hash = hash * 31 + hash(obj2);
        hash = hash * 31 + hash(obj3);
        return hash;
    }
    public static int hash(Object obj1, Object obj2, Object obj3, Object obj4){
        int hash = 1;
        hash = hash * 31 + hash(obj1);
        hash = hash * 31 + hash(obj2);
        hash = hash * 31 + hash(obj3);
        hash = hash * 31 + hash(obj4);
        return hash;
    }
    public static int hash(Object obj1, Object obj2, Object obj3, Object obj4, Object obj5){
        int hash = 1;
        hash = hash * 31 + hash(obj1);
        hash = hash * 31 + hash(obj2);
        hash = hash * 31 + hash(obj3);
        hash = hash * 31 + hash(obj4);
        hash = hash * 31 + hash(obj5);
        return hash;
    }
}

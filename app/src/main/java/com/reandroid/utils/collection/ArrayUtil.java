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
package com.reandroid.utils.collection;

public class ArrayUtil {

    public static void reverse(Object[] elements) {
        reverse(elements, 0, elements.length);
    }
    public static void reverse(Object[] elements, int start, int length) {
        int end = start + length;
        int mid = end / 2;
        end = end - 1;
        for(int i = start; i < mid; i++) {
            Object obj1 = elements[i];
            int j = end - i;
            elements[i] = elements[j];
            elements[j] = obj1;
        }
    }
}

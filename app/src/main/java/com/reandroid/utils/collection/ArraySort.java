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

import java.util.Comparator;

public class ArraySort {

    public static boolean sort(int[] elements, int start, int length){
        IntSort sorter = new IntSort(elements, start, length);
        return sorter.sort();
    }
    public static<T> boolean sort(Object[] elements, Comparator<T> comparator){
        ObjectSort sorter = new ObjectSort(elements, 0, elements.length, comparator);
        return sorter.sort();
    }
    public static boolean sort(Object[] elements, Comparator<?> comparator, int length){
        ObjectSort sorter = new ObjectSort(elements, 0, length, comparator);
        return sorter.sort();
    }
    public static boolean sort(Object[] elements, int start, int length, Comparator<?> comparator){
        ObjectSort sorter = new ObjectSort(elements, start, length, comparator);
        return sorter.sort();
    }
    public static boolean sort(int[] elements){
        IntSort sorter = new IntSort(elements, 0, elements.length);
        return sorter.sort();
    }

    public static class IntSort extends Sorter {

        private final int[] elementData;
        private final int start;
        private final int length;

        private int mid;

        public IntSort(int[] elementData, int start, int length){
            this.elementData = elementData;
            this.start = start;
            this.length = length;
        }

        public boolean sort(){
            return sort(start, length);
        }

        @Override
        public void setMid(int i) {
            this.mid = elementData[i];
        }
        @Override
        public int compareToMid(int i) {
            int data = elementData[i];
            int mid = this.mid;
            if(data == mid){
                return 0;
            }
            if(data < mid){
                return -1;
            }
            return 1;
        }
        @Override
        public void onSwap(int i, int j) {
            int[] elementData = this.elementData;
            int temp = elementData[i];
            elementData[i] = elementData[j];
            elementData[j] = temp;
        }
    }
    public static class ObjectSort extends Sorter {

        private final Object[] elementData;
        private final Comparator<Object> comparator;
        private final int start;
        private final int length;

        private Object mid;

        @SuppressWarnings("unchecked")
        public ObjectSort(Object[] elementData, int start, int length, Comparator<?> comparator){
            this.elementData = elementData;
            this.comparator = (Comparator<Object>) comparator;
            this.start = start;
            this.length = length;
        }
        public ObjectSort(Object[] elementData, Comparator<?> comparator){
            this(elementData, 0, elementData.length, comparator);
        }

        public boolean sort(){
            return sort(start, length);
        }

        @Override
        public void setMid(int i) {
            this.mid = elementData[i];
        }
        @Override
        public int compareToMid(int i) {
            return comparator.compare(elementData[i], mid);
        }
        @Override
        public void onSwap(int i, int j) {
            Object[] elementData = this.elementData;
            Object temp = elementData[i];
            elementData[i] = elementData[j];
            elementData[j] = temp;
        }
    }
}

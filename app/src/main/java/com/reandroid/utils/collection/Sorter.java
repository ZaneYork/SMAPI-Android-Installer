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

public abstract class Sorter {
    private boolean sorted;

    public boolean sort(int start, int length){
        this.sorted = false;
        runSort(start, length - 1);
        return this.sorted;
    }

    private void runSort(int begin, int end) {
        if(end <= begin){
            return;
        }
        int i = begin;
        int j = end;
        int mid = begin + (end - begin) / 2;
        setMid(mid);
        if(i == mid) {
            if(compareToMid(j) < 0) {
                onSwap(i, j);
                sorted = true;
            }
            return;
        }
        while (i <= j) {
            while (compareToMid(i) < 0) {
                i++;
            }
            while (compareToMid(j) > 0) {
                j--;
            }
            if (i <= j) {
                if(i != j){
                    onSwap(i, j);
                    sorted = true;
                }
                i++;
                j--;
            }
        }
        if (begin < j) {
            runSort(begin, j);
        }
        if (i < end) {
            runSort(i, end);
        }
    }

    public abstract void setMid(int i);
    public abstract int compareToMid(int i);
    public abstract void onSwap(int i, int j);
}

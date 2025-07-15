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
package com.reandroid.arsc.item;

public class NumberIntegerReference implements IntegerReference {
    private int value;

    public NumberIntegerReference(int value){
        this.value = value;
    }
    public NumberIntegerReference(){
        this(0);
    }

    @Override
    public int get() {
        return this.value;
    }
    @Override
    public void set(int value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return Integer.toString(get());
    }
}

/*
 *  Copyright (C) 2023 github.com/REAndroid
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
package org.jf.dexlib2.dexbacked.model;

public class DexString {
    private final DexStringSection stringSection;
    private final int index;
    private String value;

    public DexString(DexStringSection stringSection, int index, String value){
        this.stringSection = stringSection;
        this.index = index;
        this.value = value;
    }

    DexStringSection getStringSection() {
        return stringSection;
    }
    public int getIndex() {
        return index;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DexString dexString = (DexString) obj;
        return index == dexString.index && stringSection == dexString.stringSection;
    }
    @Override
    public int hashCode() {
        return index;
    }
    @Override
    public String toString(){
        return index + ": " + value;
    }
}

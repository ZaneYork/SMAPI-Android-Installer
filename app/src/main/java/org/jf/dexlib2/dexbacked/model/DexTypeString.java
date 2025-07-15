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

public class DexTypeString {
    private final DexString dexString;
    private final int index;

    public DexTypeString(DexString dexString, int index){
        this.dexString = dexString;
        this.index = index;
    }
    DexString getDexString() {
        return dexString;
    }
    public int getIndex() {
        return index;
    }
    public String getValue() {
        return dexString.getValue();
    }
    public void setValue(String value) {
        this.dexString.setValue(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DexTypeString typeString = (DexTypeString) obj;
        return index == typeString.index &&
                typeString.getDexString().getStringSection() == dexString.getStringSection();
    }
    @Override
    public int hashCode() {
        return index;
    }
    @Override
    public String toString(){
        return index + ": " + getValue();
    }
}

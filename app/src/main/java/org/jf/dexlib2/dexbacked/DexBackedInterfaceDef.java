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
package org.jf.dexlib2.dexbacked;

import org.jf.dexlib2.dexbacked.model.DexTypeString;
import org.jf.dexlib2.dexbacked.raw.ClassDefItem;

public class DexBackedInterfaceDef {
    private final DexBackedClassDef classDef;
    private final int index;

    public DexBackedInterfaceDef(DexBackedClassDef classDef, int index){
        this.classDef = classDef;
        this.index = index;
    }
    public String getType() {
        DexTypeString dexTypeString = getDexTypeString();
        return dexTypeString.getValue();
    }
    public DexTypeString getDexTypeString() {
        DexBuffer dexBuffer = classDef.dexFile.getBuffer();
        final int interfacesOffset =
                dexBuffer.readSmallUint(classDef.classDefOffset + ClassDefItem.INTERFACES_OFFSET);
        return classDef.dexFile.getTypeStringSection().get(
                dexBuffer.readUshort(
                        interfacesOffset + 4 + (2 * this.index)));
    }
}

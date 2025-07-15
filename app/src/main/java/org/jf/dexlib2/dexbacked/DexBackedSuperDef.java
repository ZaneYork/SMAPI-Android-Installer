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

public class DexBackedSuperDef {
    private final DexBackedClassDef classDef;

    public DexBackedSuperDef(DexBackedClassDef classDef){
        this.classDef = classDef;
    }

    public String getType() {
        DexTypeString dexTypeString = getDexTypeString();
        if(dexTypeString != null){
            return dexTypeString.getValue();
        }
        return null;
    }
    public DexTypeString getDexTypeString() {
        return classDef.dexFile.getTypeStringSection().get(getIndex());
    }
    private int getIndex() {
        return classDef.dexFile.getBuffer().readOptionalUint(classDef.classDefOffset
                        + ClassDefItem.SUPERCLASS_OFFSET);
    }
}

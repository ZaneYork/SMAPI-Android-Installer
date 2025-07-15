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
package com.reandroid.graph.cleaners;

import com.reandroid.apk.ApkModule;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.model.*;
import com.reandroid.graph.ApkBuildOption;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.Iterator;
import java.util.List;

public class UnusedFieldsCleaner extends UnusedClassComponentCleaner<DexField> {

    public UnusedFieldsCleaner(ApkBuildOption buildOption, ApkModule apkModule,
                               DexClassRepository classRepository) {
        super(buildOption, apkModule, classRepository);
    }

    @Override
    protected boolean isEnabled() {
        return getBuildOption().isMinifyFields();
    }
    @Override
    protected List<DexField> listUnusedInClass(DexClass dexClass) {
        Iterator<DexField> iterator = dexClass.getDeclaredFields();
        ArrayCollection<DexField> list = null;
        while (iterator.hasNext()) {
            DexField dexField = iterator.next();
            if(isUnusedField(dexField)) {
                if(list == null) {
                    list = new ArrayCollection<>();
                }
                list.add(dexField);
            }
        }
        return list;
    }

    private boolean isUnusedField(DexField dexField) {
        return isUnusedPrivateField(dexField) || isUnusedInstanceField(dexField);
    }
    private boolean isUnusedPrivateField(DexField dexField) {
        if(!dexField.isPrivate()) {
            return false;
        }
        FieldKey fieldKey = dexField.getKey();
        String name = fieldKey.getName();
        Iterator<DexInstruction> iterator = dexField.getDexClass().getDexInstructions();
        while (iterator.hasNext()) {
            DexInstruction instruction = iterator.next();
            if(fieldKey.equals(instruction.getFieldKey()) || name.equals(instruction.getString())) {
                return false;
            }
        }
        return true;
    }
    private boolean isUnusedInstanceField(DexField dexField) {
        // TODO:
        return false;
    }
}

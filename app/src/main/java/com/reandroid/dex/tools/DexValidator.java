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
package com.reandroid.dex.tools;

import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.sections.SectionType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DexValidator {

    private final DexClassRepository classRepository;

    public DexValidator(DexClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public Set<FieldKey> findMissingOrInaccessibleFields() {
        Set<FieldKey> results = new HashSet<>();
        DexClassRepository classRepository = getClassRepository();
        Iterator<FieldId> iterator = classRepository.getItems(SectionType.FIELD_ID);
        while (iterator.hasNext()) {
            FieldKey key = iterator.next().getKey();
            if(results.contains(key)) {
                continue;
            }
            DexClass declaring = classRepository.getDexClass(key.getDeclaring());
            if(declaring != null && declaring.getField(key) == null) {
                results.add(key);
            }
        }
        return results;
    }
    public Set<MethodKey> findMissingOrInaccessibleMethods() {
        Set<MethodKey> results = new HashSet<>();
        DexClassRepository classRepository = getClassRepository();
        Iterator<MethodId> iterator = classRepository.getItems(SectionType.METHOD_ID);
        while (iterator.hasNext()) {
            MethodKey key = iterator.next().getKey();
            if(results.contains(key)) {
                continue;
            }
            DexClass declaring = classRepository.getDexClass(key.getDeclaring());
            if(declaring != null && declaring.getMethod(key) == null) {
                results.add(key);
            }
        }
        return results;
    }

    private DexClassRepository getClassRepository() {
        return classRepository;
    }
}

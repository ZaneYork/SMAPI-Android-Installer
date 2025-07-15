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
package com.reandroid.graph;

import com.reandroid.apk.ApkModule;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RequiredClassesScanner extends BaseApkModuleProcessor {

    private final VitalClassesSet vitalClassesSet;
    private final Set<TypeKey> requiredTypes;

    private boolean lookInStrings = true;

    public RequiredClassesScanner(VitalClassesSet vitalClassesSet, ApkModule apkModule, DexClassRepository classRepository) {
        super(apkModule, classRepository);
        this.vitalClassesSet = vitalClassesSet;
        this.requiredTypes = new HashSet<>();
    }

    public void setLookInStrings(boolean lookInStrings) {
        this.lookInStrings = lookInStrings;
    }
    @Override
    public void apply() {
        debug("Scanning required classes ...");
        addVitalClasses();
        scanOnStrings();
    }
    public Set<TypeKey> getResults() {
        return requiredTypes;
    }

    private void addVitalClasses() {
        VitalClassesSet vitalClassesSet = this.vitalClassesSet;
        vitalClassesSet.apply();
        DexClassRepository repository = getClassRepository();
        Iterator<TypeKey> iterator = vitalClassesSet.getMainClasses();
        while (iterator.hasNext()) {
            addUsed(repository.getDexClass(iterator.next()));
        }
    }
    private void scanOnStrings() {
        if(!this.lookInStrings) {
            return;
        }
        scanOnDexStrings();
    }
    private void scanOnDexStrings() {
        if(keptAll()) {
            return;
        }
        debug("Searching on dex strings ...");
        VitalClassesSet vitalClassesSet = this.vitalClassesSet;
        vitalClassesSet.updateSourceStrings();
        DexClassRepository repository = getClassRepository();
        Iterator<DexClass> iterator = repository.getDexClasses(vitalClassesSet::containsSourceString);
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            addUsed(dexClass);
            debug(dexClass.getKey().getSourceName());
        }
    }
    private void addUsed(DexClass dexClass) {
        if(dexClass == null) {
            return;
        }
        TypeKey typeKey = dexClass.getKey();
        Set<TypeKey> requiredTypes = this.requiredTypes;
        if(requiredTypes.contains(typeKey)) {
            return;
        }
        Set<DexClass> requiredSet = dexClass.getRequired(key -> !requiredTypes.contains(key));
        requiredTypes.add(typeKey);
        for(DexClass dex : requiredSet) {
            requiredTypes.add(dex.getKey());
        }
    }

    private boolean keptAll() {
        return requiredTypes.size() == getClassRepository().getDexClassesCount();
    }
    public void reset() {
        this.requiredTypes.clear();
    }
}

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
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.graph.ApkBuildOption;
import com.reandroid.graph.RequiredClassesScanner;
import com.reandroid.graph.VitalClassesSet;

import java.util.Iterator;
import java.util.Set;

public class UnusedClassesCleaner extends UnusedCleaner<DexClass> {

    private VitalClassesSet vitalClassesSet;

    public UnusedClassesCleaner(ApkBuildOption buildOption, ApkModule apkModule,
                                DexClassRepository classRepository) {
        super(buildOption, apkModule, classRepository);
    }

    @Override
    public void apply() {
        if(!isEnabled()) {
            debug("Skip");
            return;
        }
        getVitalClassesSet().setReporter(getReporter()).apply();
        cleanCyclic();
    }

    public VitalClassesSet getVitalClassesSet() {
        VitalClassesSet vitalClassesSet = this.vitalClassesSet;
        if(vitalClassesSet == null) {
            vitalClassesSet = new VitalClassesSet(getBuildOption(),
                    getApkModule(), getClassRepository());
            this.vitalClassesSet = vitalClassesSet;
        }
        return vitalClassesSet;
    }
    public void setVitalClassesSet(VitalClassesSet vitalClassesSet) {
        this.vitalClassesSet = vitalClassesSet;
    }

    private void cleanCyclic() {
        int cycle = 0;
        int totalRemoved = 0;
        int removedCount = 1;
        while(cycle < MAXIMUM_CYCLE && removedCount > 0) {
            cycle ++;
            Set<TypeKey> requiredClasses = scanRequiredClasses();
            debugReportClassesToRemove(requiredClasses);
            removedCount = cleanUnusedClasses(requiredClasses);
            totalRemoved += removedCount;
            verbose("Cycle: " + cycle + ", removed: " + removedCount + ", total: " + totalRemoved);
        }
        setCount(totalRemoved);
    }
    private int cleanUnusedClasses(Set<TypeKey> requiredClasses) {
        DexClassRepository repository = getClassRepository();
        int previousCount = repository.getDexClassesCount();
        repository.removeClassesWithKeys(typeKey -> !requiredClasses.contains(typeKey));
        int removed = previousCount - repository.getDexClassesCount();
        setCount(removed);
        if(removed != 0) {
            repository.shrink();
        }
        return removed;
    }
    private void debugReportClassesToRemove(Set<TypeKey> requiredClasses) {
        if(!isDebugEnabled()) {
            return;
        }
        DexClassRepository repository = getClassRepository();
        Iterator<DexClass> iterator = repository.getDexClasses(typeKey -> !requiredClasses.contains(typeKey));
        while (iterator.hasNext()) {
            debug(iterator.next().getKey().toString());
        }
    }
    private Set<TypeKey> scanRequiredClasses() {
        RequiredClassesScanner scanner = new RequiredClassesScanner(
                this.vitalClassesSet,
                getApkModule(),
                getClassRepository());
        scanner.setReporter(getReporter());
        scanner.setLookInStrings(getBuildOption().isProcessClassNamesOnStrings());
        scanner.apply();
        return scanner.getResults();
    }
    @Override
    protected boolean isEnabled() {
        return getBuildOption().isMinifyClasses();
    }

    private static final int MAXIMUM_CYCLE = 25;
}

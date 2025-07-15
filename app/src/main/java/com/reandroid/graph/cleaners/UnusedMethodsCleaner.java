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
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.model.DexInstruction;
import com.reandroid.dex.model.DexMethod;
import com.reandroid.graph.ApkBuildOption;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UnusedMethodsCleaner extends UnusedClassComponentCleaner<DexMethod> {

    private Set<MethodKey> unusedInternalMethods;

    public UnusedMethodsCleaner(ApkBuildOption buildOption, ApkModule apkModule, DexClassRepository classRepository) {
        super(buildOption, apkModule, classRepository);
    }

    @Override
    protected boolean isEnabled() {
        return getBuildOption().isMinifyMethods();
    }
    @Override
    protected List<DexMethod> listUnusedInClass(DexClass dexClass) {
        Iterator<DexMethod> iterator = dexClass.getDeclaredMethods();
        ArrayCollection<DexMethod> list = null;
        while (iterator.hasNext()) {
            DexMethod dexMethod = iterator.next();
            if(isUnusedMethod(dexMethod)) {
                if(list == null) {
                    list = new ArrayCollection<>();
                }
                list.add(dexMethod);
            }
        }
        return list;
    }
    private boolean isUnusedMethod(DexMethod dexMethod) {
        if(dexMethod.isConstructor() && dexMethod.isStatic()) {
            return false;
        }
        return isUnusedPrivateMethod(dexMethod) || isUnusedVirtualMethod(dexMethod);
    }
    private boolean isUnusedPrivateMethod(DexMethod dexMethod) {
        if(!dexMethod.isPrivate() || dexMethod.isConstructor()) {
            return false;
        }
        MethodKey methodKey = dexMethod.getKey();
        Iterator<DexInstruction> iterator = dexMethod.getDexClass().getDexInstructions();
        while (iterator.hasNext()) {
            DexInstruction instruction = iterator.next();
            if(methodKey.equals(instruction.getMethodKey())) {
                return false;
            }
        }
        return true;
    }
    private boolean isUnusedVirtualMethod(DexMethod dexMethod) {
        // TODO:
        return getUnusedInternalMethods().contains(dexMethod.getKey());
    }

    public Set<MethodKey> getUnusedInternalMethods() {
        Set<MethodKey> unusedInternalMethods = this.unusedInternalMethods;
        if(unusedInternalMethods == null) {
            loadUnusedInternalMethods();
            unusedInternalMethods = this.unusedInternalMethods;
        }
        return unusedInternalMethods;
    }
    private void loadUnusedInternalMethods() {
        Set<MethodKey> unusedInternalMethods = new HashSet<>();
        Iterator<DexClass> iterator = getClassRepository().getDexClasses();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            if(dexClass.usesNative()) {
                continue;
            }
            Iterator<DexMethod> methods = dexClass.getDeclaredMethods();
            while (methods.hasNext()) {
                DexMethod dexMethod = methods.next();
                if(isInternal(dexMethod)) {
                    unusedInternalMethods.add(dexMethod.getKey());
                }
            }
        }
        subtractUnused(unusedInternalMethods);
        this.unusedInternalMethods = unusedInternalMethods;
        for(MethodKey key : unusedInternalMethods) {
            debug(key.toString());
        }
        debug("Internal methods: " + unusedInternalMethods.size());
    }
    private void subtractUnused(Set<MethodKey> unusedInternalMethods) {
        if(unusedInternalMethods.isEmpty()) {
            return;
        }
        DexClassRepository repository = getClassRepository();
        Iterator<DexClass> iterator = repository.getDexClasses();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            Iterator<DexInstruction> instructionIterator = dexClass.getDexInstructions();
            while (instructionIterator.hasNext()) {
                DexInstruction instruction = instructionIterator.next();
                MethodKey key = instruction.getMethodKey();
                if(key != null) {
                   unusedInternalMethods.remove(key);
                    Iterator<MethodKey> equivalents = repository.findEquivalentMethods(key);
                    while (equivalents.hasNext()) {
                        unusedInternalMethods.remove(equivalents.next());
                    }
                }
            }
        }
    }
    private boolean isInternal(DexMethod dexMethod) {
        if(dexMethod.isConstructor()) {
            return false;
        }
        if(!dexMethod.isInternal() && !dexMethod.isStatic()) {
            return false;
        }
        if(dexMethod.getSuperMethods().hasNext() || dexMethod.getExtending().hasNext()) {
            return false;
        }
        return true;
    }
}

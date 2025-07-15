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
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.model.DexInstruction;
import com.reandroid.graph.ApkBuildOption;
import com.reandroid.utils.collection.FilterIterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UnusedAnnotationCleaner extends UnusedCleaner<DexClass> {

    public UnusedAnnotationCleaner(ApkBuildOption buildOption,
                                   ApkModule apkModule, DexClassRepository classRepository) {
        super(buildOption, apkModule, classRepository);
    }

    @Override
    public void apply() {
        if(!isEnabled()) {
            debug("Skip");
            return;
        }
        Set<TypeKey> unusedAnnotations = findUnusedAnnotations();
        cleanAnnotations(unusedAnnotations);
    }
    private void cleanAnnotations(Set<TypeKey> unusedAnnotations) {
        DexClassRepository repository = getClassRepository();
        for(TypeKey typeKey : unusedAnnotations) {
            if(repository.removeAnnotations(typeKey)) {
                addCount();
            }
        }
    }
    private Set<TypeKey> findUnusedAnnotations() {
        verbose("Searching ...");
        Set<TypeKey> targetAnnotations = new HashSet<>();
        Iterator<DexClass> iterator = getTargetAnnotations();
        while (iterator.hasNext()) {
            targetAnnotations.add(iterator.next().getKey());
        }
        subtractUnused(targetAnnotations);
        logUnused(targetAnnotations);
        verbose("Unused annotations: " + targetAnnotations.size());
        return targetAnnotations;
    }

    private void logUnused(Set<TypeKey> unusedAnnotations) {
        if(isDebugEnabled()) {
            for(TypeKey typeKey : unusedAnnotations) {
                debug(typeKey.getTypeName());
            }
        }
    }
    private void subtractUnused(Set<TypeKey> targetAnnotations) {
        Iterator<DexClass> iterator = getClassRepository().getDexClasses();
        while (!targetAnnotations.isEmpty() && iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            Iterator<DexInstruction> instructions = dexClass.getDexInstructions();
            while (instructions.hasNext()) {
                subtractUnused(targetAnnotations, instructions.next());
            }
        }
    }
    private void subtractUnused(Set<TypeKey> targetAnnotations, DexInstruction userInstruction) {
        Key key = userInstruction.getKey();
        if(key != null) {
            Iterator<? extends Key> iterator = key.mentionedKeys();
            while (iterator.hasNext()) {
                TypeKey typeKey = iterator.next().getDeclaring();
                targetAnnotations.remove(typeKey);
            }
        }
    }
    private Iterator<DexClass> getTargetAnnotations() {
        return FilterIterator.of(getClassRepository().getDexClasses(),
                DexClass::isAnnotation);
    }

    @Override
    protected boolean isEnabled() {
        return getBuildOption().isCleanAnnotations();
    }
}

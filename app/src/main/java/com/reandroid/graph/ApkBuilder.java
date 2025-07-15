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
import com.reandroid.apk.ResFile;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.model.ResourceName;
import com.reandroid.arsc.refactor.ResourceBuilder;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.graph.cleaners.UnusedAnnotationCleaner;
import com.reandroid.graph.cleaners.UnusedClassesCleaner;
import com.reandroid.graph.cleaners.UnusedFieldsCleaner;
import com.reandroid.graph.cleaners.UnusedMethodsCleaner;
import com.reandroid.utils.collection.CollectionUtil;

import java.util.List;
import java.util.Set;

public class ApkBuilder extends BaseApkModuleProcessor {

    private ApkBuildOption buildOption;
    private VitalClassesSet vitalClassesSet;

    public ApkBuilder(ApkModule sourceModule, DexClassRepository classRepository) {
        super(sourceModule, classRepository);
    }

    @Override
    public void apply() {

        int filesCount = getApkModule().getZipEntryMap().size();

        resolveInlineIntegerFieldCalls();

        cleanDex();

        ResourceMergeOption resourceMergeOption = getBuildOption().getResourceMergeOption();
        ResourceBuilder resourceBuilder = new ResourceBuilder(resourceMergeOption,
                getApkModule().getTableBlock());

        cleanUnusedResFiles();
        initializeRequiredIds();

        resourceBuilder.rebuild();

        ApkModule sourceModule = getApkModule();
        resourceBuilder.rebuildManifest(sourceModule);
        resourceBuilder.applyIdChanges(getClassRepository().visitIntegers());
        ApkModule resultModule = resourceBuilder.getResultModule();

        removeResFiles();

        sourceModule.setTableBlock(resultModule.getTableBlock());
        sourceModule.keepTableBlockChanges();

        resultModule.setTableBlock(null);
        sourceModule.getZipEntryMap().addAll(resultModule.getZipEntryMap());

        filesCount = filesCount - sourceModule.getZipEntryMap().size();
        verbose("Removed files: " + filesCount);
    }
    private void cleanDex() {
        int countTotal = 0;
        int count = 1;
        int cycle = 0;
        while (cycle < MAX_CYCLE && count > 0) {
            cycle ++;
            verbose("Stage " + cycle);
            count = cyclicCleanDex();
            countTotal += count;
        }
        verbose("Cleaned dex: " + countTotal);
    }
    private int cyclicCleanDex() {
        int count = 0;
        UnusedFieldsCleaner fieldsCleaner = new UnusedFieldsCleaner(getBuildOption(),
                getApkModule(), getClassRepository());
        fieldsCleaner.setReporter(getReporter());
        fieldsCleaner.apply();
        count += fieldsCleaner.getCount();

        UnusedMethodsCleaner methodsCleaner = new UnusedMethodsCleaner(getBuildOption(),
                getApkModule(), getClassRepository());
        methodsCleaner.setReporter(getReporter());
        methodsCleaner.apply();
        count += methodsCleaner.getCount();

        UnusedAnnotationCleaner unusedAnnotationCleaner = new UnusedAnnotationCleaner(getBuildOption(),
                getApkModule(), getClassRepository());
        unusedAnnotationCleaner.setReporter(getReporter());
        unusedAnnotationCleaner.apply();
        count += unusedAnnotationCleaner.getCount();

        UnusedClassesCleaner classesCleaner = new UnusedClassesCleaner(getBuildOption(),
                getApkModule(), getClassRepository());
        classesCleaner.setReporter(getReporter());
        classesCleaner.setVitalClassesSet(this.vitalClassesSet);
        classesCleaner.apply();
        this.vitalClassesSet = classesCleaner.getVitalClassesSet();
        count += classesCleaner.getCount();
        return count;
    }
    private void resolveInlineIntegerFieldCalls() {
        if(getBuildOption().isMinifyResources()) {
            InlineFieldIntResolver resolver = new InlineFieldIntResolver(
                    getClassRepository(),
                    getApkModule().getTableBlock());
            resolver.setReporter(getReporter());
            resolver.apply();
        }
    }
    private void cleanUnusedResFiles() {
        if(!getBuildOption().isMinifyResources()) {
            return;
        }
        ApkModule apkModule = getApkModule();

        RequiredEntriesScanner scanner = new RequiredEntriesScanner(getBuildOption(),
                apkModule, getClassRepository());
        scanner.apply();

        Set<String> requiredFiles = scanner.getRequiredFiles();
        List<ResFile> resFileList = apkModule.listResFiles();
        ZipEntryMap zipEntryMap = apkModule.getZipEntryMap();
        for(ResFile resFile : resFileList) {
            String path = resFile.getFilePath();
            if(!requiredFiles.contains(path)) {
                zipEntryMap.remove(path);
            }
        }
    }
    private void removeResFiles() {
        if(!getBuildOption().isMinifyResources()) {
            return;
        }
        ApkModule apkModule = getApkModule();
        List<ResFile> resFileList = apkModule.listResFiles();
        ZipEntryMap zipEntryMap = apkModule.getZipEntryMap();
        for(ResFile resFile : resFileList) {
            zipEntryMap.remove(resFile.getInputSource());
        }
    }
    private void initializeRequiredIds() {
        ResourceMergeOption resourceMergeOption = getBuildOption().getResourceMergeOption();
        if(!getBuildOption().isMinifyResources()) {
            resourceMergeOption.setKeepEntries(CollectionUtil.getAcceptAll());
            return;
        }
        RequiredEntriesScanner scanner = new RequiredEntriesScanner(getBuildOption(),
                getApkModule(), getClassRepository());
        scanner.setReporter(getReporter());
        scanner.apply();
        Set<ResourceName> requiredResources = scanner.getRequiredResources();
        resourceMergeOption.setKeepEntries(resourceEntry ->
                requiredResources.contains(resourceEntry.toResourceName()));
    }
    public ApkBuildOption getBuildOption() {
        ApkBuildOption buildOption = this.buildOption;
        if(buildOption == null) {
            buildOption = new ApkBuildOption();
            this.buildOption = buildOption;
        }
        return buildOption;
    }
    public void setBuildOption(ApkBuildOption buildOption) {
        this.buildOption = buildOption;
    }
    private static final int MAX_CYCLE = 25;
}

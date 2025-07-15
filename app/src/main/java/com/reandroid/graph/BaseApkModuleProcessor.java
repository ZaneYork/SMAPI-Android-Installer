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
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.dex.model.DexClassRepository;

public abstract class BaseApkModuleProcessor extends BaseDexClassProcessor {

    private final ApkModule apkModule;

    public BaseApkModuleProcessor(ApkModule apkModule, DexClassRepository classRepository) {
        super(classRepository);
        this.apkModule = apkModule;
    }

    public ZipEntryMap getZipEntryMap() {
        return getApkModule().getZipEntryMap();
    }
    public ApkModule getApkModule() {
        return apkModule;
    }
}

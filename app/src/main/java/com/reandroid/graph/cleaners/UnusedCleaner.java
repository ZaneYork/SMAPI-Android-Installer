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
import com.reandroid.dex.model.Dex;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.graph.ApkBuildOption;
import com.reandroid.graph.BaseApkModuleProcessor;

public abstract class UnusedCleaner<T extends Dex> extends BaseApkModuleProcessor {

    private final ApkBuildOption buildOption;

    private int mCount;

    public UnusedCleaner(ApkBuildOption buildOption, ApkModule apkModule, DexClassRepository classRepository) {
        super(apkModule, classRepository);
        this.buildOption = buildOption;
    }

    protected abstract boolean isEnabled();
    public ApkBuildOption getBuildOption() {
        return buildOption;
    }

    public int getCount() {
        return mCount;
    }
    public void addCount() {
        mCount ++;
    }
    protected void setCount(int count) {
        this.mCount = count;
    }
}

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
package com.reandroid.apk;

import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

public class DexFileRawEncoder implements DexEncoder {
    private APKLogger apkLogger;
    public DexFileRawEncoder(){
    }

    @Override
    public List<InputSource> buildDexFiles(ApkModuleEncoder encoder, File mainDirectory) throws IOException {
        File dexDir = new File(mainDirectory,
                DexFileInputSource.DEX_DIRECTORY_NAME);
        if(!dexDir.isDirectory()){
            return new ArrayList<>();
        }
        List<File> dexFileList = DexFileInputSource.listDexFiles(dexDir);
        List<InputSource> results = new ArrayList<>(dexFileList.size());
        if(dexFileList.size() == 0){
            logMessage("WARN: No dex files found on: " + dexDir);
            return results;
        }
        for(File file : dexFileList){
            String name = file.getName();
            FileInputSource fileInputSource = new FileInputSource(file, name);
            fileInputSource.setMethod(ZipEntry.STORED);
            DexFileInputSource inputSource = new DexFileInputSource(name, fileInputSource);
            inputSource.setMethod(ZipEntry.STORED);
            results.add(inputSource);
            logVerbose(name);
        }
        return results;
    }

    public void setApkLogger(APKLogger apkLogger) {
        this.apkLogger = apkLogger;
    }
    APKLogger getApkLogger() {
        return apkLogger;
    }
    void logMessage(String msg) {
        APKLogger apkLogger = this.apkLogger;
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    void logError(String msg, Throwable tr) {
        APKLogger apkLogger = this.apkLogger;
        if(apkLogger == null || (msg == null && tr == null)){
            return;
        }
        apkLogger.logError(msg, tr);
    }
    void logVerbose(String msg) {
        APKLogger apkLogger = this.apkLogger;
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
}

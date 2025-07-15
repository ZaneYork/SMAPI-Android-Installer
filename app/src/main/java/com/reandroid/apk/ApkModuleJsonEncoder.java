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

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ApkModuleJsonEncoder extends ApkModuleEncoder {

    private ApkModule apkModule;
    public ApkModuleJsonEncoder(){
        super();
    }

    @Override
    public void buildResources(File mainDirectory) throws IOException {
        scanManifest(mainDirectory);
        scanTable(mainDirectory);
        scanResJsonDirs(mainDirectory);
    }

    @Override
    public ApkModule getApkModule() {
        if(apkModule == null){
            apkModule = new ApkModule();
            apkModule.setLoadDefaultFramework(false);
            apkModule.setAPKLogger(getApkLogger());
        }
        return apkModule;
    }
    private void scanResJsonDirs(File mainDirectory){
        File resJsonDir = new File(mainDirectory, TableBlock.RES_JSON_DIRECTORY_NAME);
        List<File> jsonFileList = ApkUtil.recursiveFiles(resJsonDir);
        for(File file:jsonFileList){
            scanResJsonFile(resJsonDir, file);
        }
    }
    private void scanResJsonFile(File resJsonDir, File file){
        JsonXmlInputSource inputSource = JsonXmlInputSource.fromFile(resJsonDir, file);
        getApkModule().add(inputSource);
    }
    private void scanManifest(File mainDirectory){
        File file = new File(mainDirectory, AndroidManifestBlock.FILE_NAME_JSON);
        if(!file.isFile()){
            return;
        }
        JsonManifestInputSource inputSource=JsonManifestInputSource.fromFile(mainDirectory, file);
        inputSource.setAPKLogger(getApkLogger());
        getApkModule().add(inputSource);
    }
    private void scanTable(File mainDirectory) throws IOException{
        boolean singleFound = scanTableSingleJson(mainDirectory);
        if(singleFound){
            logMessage("Building as single json");
            return;
        }
        boolean splitFound = scanTableSplitJson(mainDirectory);
        if(splitFound){
            logMessage("Building as split json");
            return;
        }
        if(getApkModule().hasTableBlock()){
            logMessage("WARN: Can not determine json type! " +
                    "Ignore building resource table");
            return;
        }
        throw new IOException("Can not determine json type! main directory '" +
                mainDirectory.getAbsolutePath() + "'");
    }
    private boolean scanTableSplitJson(File mainDirectory) {
        File resourcesDir = new File(mainDirectory, TableBlock.DIRECTORY_NAME);
        if(!resourcesDir.isDirectory()){
            return false;
        }
        SplitJsonTableInputSource inputSource = new SplitJsonTableInputSource(resourcesDir);
        getApkModule().add(inputSource);
        return true;
    }
    private boolean scanTableSingleJson(File mainDirectory) {
        File resourcesDir = new File(mainDirectory, TableBlock.DIRECTORY_NAME);
        File file = new File(resourcesDir, TableBlock.JSON_FILE_NAME);
        if(!file.isFile()){
            return false;
        }
        SingleJsonTableInputSource inputSource = SingleJsonTableInputSource
                .fromFile(resourcesDir, file);
        getApkModule().add(inputSource);
        return true;
    }

}

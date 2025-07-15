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

import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ApkModuleJsonDecoder extends ApkModuleDecoder{
    private final boolean splitTypes;

    public ApkModuleJsonDecoder(ApkModule apkModule, boolean splitTypes){
        super(apkModule);
        this.splitTypes = splitTypes;
    }
    public ApkModuleJsonDecoder(ApkModule apkModule){
        this(apkModule, false);
    }
    @Override
    public void decodeResourceTable(File mainDirectory) throws IOException {
        decodeTable(mainDirectory);
        decodeResFiles(mainDirectory);
    }
    private void decodeResFiles(File mainDirectory) throws IOException {
        ApkModule apkModule = getApkModule();
        logMessage("Decoding res files ...");
        for(ResFile resFile:apkModule.listResFiles()){
            decodeResFile(mainDirectory, resFile);
        }
    }
    private void decodeResFile(File mainDirectory, ResFile resFile) throws IOException {
        if(resFile.isBinaryXml()){
            decodeResFileXml(mainDirectory, resFile);
        }
    }
    private void decodeResFileXml(File mainDirectory, ResFile resFile) throws IOException {
        InputSource inputSource = resFile.getInputSource();
        String path = inputSource.getAlias();
        logVerbose(path);
        File file = toResJson(mainDirectory, path);
        ResXmlDocument resXmlDocument = new ResXmlDocument();
        resXmlDocument.readBytes(inputSource.openStream());
        JSONObject jsonObject = resXmlDocument.toJson();
        jsonObject.write(file);
        addDecodedPath(path);
    }
    private void decodeTable(File dir) throws IOException {
        if(!splitTypes){
            writeTableSingle(dir);
            return;
        }
        writeTableSplit(dir);
    }
    private void writeTableSplit(File dir) throws IOException {
        ApkModule apkModule = getApkModule();
        if(!apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        File resourcesDir = new File(dir, TableBlock.DIRECTORY_NAME);
        SplitJsonResourceDecoder splitJsonResourceDecoder = new SplitJsonResourceDecoder(tableBlock);
        splitJsonResourceDecoder.decodeSplitJsonFiles(resourcesDir);
        addDecodedPath(TableBlock.FILE_NAME);
    }
    private void writeTableSingle(File mainDirectory) throws IOException {
        ApkModule apkModule = getApkModule();
        if(!apkModule.hasTableBlock()){
            return;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        File file = new File(mainDirectory, TableBlock.DIRECTORY_NAME);
        file = new File(file, TableBlock.FILE_NAME_JSON);
        tableBlock.toJson().write(file);
        addDecodedPath(TableBlock.FILE_NAME);
    }
    void decodeAndroidManifest(File mainDirectory) throws IOException {
        ApkModule apkModule = getApkModule();
        if(!apkModule.hasAndroidManifest()){
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        File file = new File(mainDirectory, AndroidManifestBlock.FILE_NAME_JSON);
        manifest.toJson().write(file);
        addDecodedPath(AndroidManifestBlock.FILE_NAME);
    }
    private File toResJson(File mainDirectory, String path){
        File file = new File(mainDirectory, TableBlock.RES_JSON_DIRECTORY_NAME);
        path = path + ApkUtil.JSON_FILE_EXTENSION;
        path = path.replace('/', File.separatorChar);
        return new File(file, path);
    }

}

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

import java.io.File;
import java.io.IOException;

public class ApkModuleRawDecoder extends ApkModuleDecoder{
    public ApkModuleRawDecoder(ApkModule apkModule) {
        super(apkModule);
        apkModule.setLoadDefaultFramework(false);
    }

    @Override
    public void decodeResourceTable(File mainDirectory) throws IOException {
        ApkModule apkModule = getApkModule();
        apkModule.discardTableBlockChanges();
        InputSource inputSource = apkModule.getTableOriginalSource();
        if(inputSource == null){
            logMessage("File NOT found: " + TableBlock.FILE_NAME);
        }else {
            File file = new File(mainDirectory, TableBlock.FILE_NAME);
            inputSource.write(file);
            addDecodedPath(TableBlock.FILE_NAME);
        }
    }

    @Override
    void decodeAndroidManifest(File mainDirectory) throws IOException {
        ApkModule apkModule = getApkModule();
        apkModule.discardManifestChanges();
        InputSource inputSource = apkModule.getManifestOriginalSource();
        if(inputSource == null){
            logMessage("File NOT found: " + AndroidManifestBlock.FILE_NAME);
        }else {
            File file = new File(mainDirectory, AndroidManifestBlock.FILE_NAME_BIN);
            inputSource.write(file);
            addDecodedPath(AndroidManifestBlock.FILE_NAME);
        }
    }
}

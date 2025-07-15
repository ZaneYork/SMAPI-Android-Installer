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

import java.io.File;
import java.io.IOException;

public class DexFileRawDecoder implements DexDecoder{
    private APKLogger apkLogger;
    public DexFileRawDecoder(){
    }

    @Override
    public void decodeDex(DexFileInputSource dexFileInputSource, File mainDirectory) throws IOException {
        logVerbose(dexFileInputSource.getAlias());
        File file = new File(mainDirectory, DEX_DIRECTORY_NAME);
        file = dexFileInputSource.toFile(file);
        dexFileInputSource.write(file);
    }

    public void setApkLogger(APKLogger apkLogger) {
        this.apkLogger = apkLogger;
    }
    private void logVerbose(String msg){
        APKLogger apkLogger = this.apkLogger;
        if(apkLogger != null){
            apkLogger.logVerbose(msg);
        }
    }
}

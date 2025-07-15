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
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;

public class JsonManifestInputSource extends JsonXmlInputSource {
    public JsonManifestInputSource(InputSource inputSource) {
        super(inputSource);
    }
    AndroidManifestBlock newInstance(){
        return new AndroidManifestBlock();
    }
    public static JsonManifestInputSource fromFile(File rootDir, File jsonFile){
        String path = ApkUtil.jsonToArchiveResourcePath(rootDir, jsonFile);
        FileInputSource fileInputSource = new FileInputSource(jsonFile, path);
        return new JsonManifestInputSource(fileInputSource);
    }
}
